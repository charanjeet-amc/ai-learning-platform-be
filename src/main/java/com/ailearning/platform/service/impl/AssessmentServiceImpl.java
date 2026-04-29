package com.ailearning.platform.service.impl;

import com.ailearning.platform.dto.request.SubmitAnswerRequest;
import com.ailearning.platform.dto.response.AnswerResultResponse;
import com.ailearning.platform.dto.response.QuestionResponse;
import com.ailearning.platform.entity.*;
import com.ailearning.platform.entity.enums.ConceptStatus;
import com.ailearning.platform.entity.enums.QuestionType;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.entity.UserLearningProfile;
import com.ailearning.platform.entity.enums.LearningPace;
import com.ailearning.platform.repository.*;
import com.ailearning.platform.ai.MasteryCalculator;
import com.ailearning.platform.ai.AdaptiveEngine;
import com.ailearning.platform.ai.MisconceptionDetector;
import com.ailearning.platform.ai.SpacedRepetitionEngine;
import com.ailearning.platform.ai.QuestionGeneratorEngine;
import com.ailearning.platform.service.AssessmentService;
import com.ailearning.platform.service.EnrollmentService;
import com.ailearning.platform.service.GamificationService;
import com.ailearning.platform.dto.response.UserProgressResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentServiceImpl implements AssessmentService {

    private final QuestionRepository questionRepository;
    private final UserAttemptRepository userAttemptRepository;
    private final UserConceptProgressRepository progressRepository;
    private final UserWeakAreaRepository weakAreaRepository;
    private final UserRepository userRepository;
    private final ConceptRepository conceptRepository;
    private final MasteryCalculator masteryCalculator;
    private final AdaptiveEngine adaptiveEngine;
    private final MisconceptionDetector misconceptionDetector;
    private final SpacedRepetitionEngine spacedRepetitionEngine;
    private final QuestionGeneratorEngine questionGeneratorEngine;
    private final GamificationService gamificationService;
    private final EnrollmentService enrollmentService;
    private final UserLearningProfileRepository learningProfileRepository;
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    private record AIEvaluation(boolean correct, double score, String feedback) {}

    @Override
    public List<QuestionResponse> getQuestionsForConcept(UUID conceptId, UUID userId) {
        UserConceptProgress progress = progressRepository
                .findByUserIdAndConceptId(userId, conceptId).orElse(null);

        List<Question> allQuestions = questionRepository.findByConceptIdForUser(conceptId, userId);

        // Filter by difficulty based on user progress, but always fall back to all questions
        List<Question> questions = allQuestions;
        if (progress != null && progress.getMasteryLevel() > 0.6) {
            List<Question> filtered = allQuestions.stream()
                    .filter(q -> q.getDifficulty().ordinal() >= progress.getMasteryLevel() * 4)
                    .collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                questions = filtered;
            }
        }

        return questions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AnswerResultResponse submitAnswer(SubmitAnswerRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", request.getQuestionId()));

        // Validate the answer
        boolean correct;
        double score;
        String aiFeedback = null;

        if (question.getType() == QuestionType.SUBJECTIVE || question.getType() == QuestionType.CODING) {
            AIEvaluation eval = evaluateWithAI(question, request.getAnswer());
            correct = eval.correct();
            score = eval.score();
            aiFeedback = eval.feedback();
        } else {
            correct = validateAnswer(question, request.getAnswer());
            score = correct ? 1.0 : 0.0;
        }

        // Misconception detection — only meaningful on wrong answers
        String detectedMisconception = null;
        if (!correct) {
            Object answerObj = request.getAnswer().get("answer");
            String answerText = answerObj != null ? answerObj.toString() : "";
            detectedMisconception = misconceptionDetector.detect(
                    answerText, question.getConcept().getMisconceptions());
        }

        // Record the attempt
        UserAttempt attempt = UserAttempt.builder()
                .user(user)
                .question(question)
                .answer(request.getAnswer())
                .score(score)
                .correct(correct)
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .hintsUsed(request.getHintsUsed() != null ? request.getHintsUsed() : 0)
                .triggeredMisconception(detectedMisconception)
                .build();
        attempt = userAttemptRepository.save(attempt);

        // Update concept progress
        UUID conceptId = question.getConcept().getId();
        UserConceptProgress progress = progressRepository
                .findByUserIdAndConceptId(userId, conceptId)
                .orElseGet(() -> UserConceptProgress.builder()
                        .user(user)
                        .concept(question.getConcept())
                        .status(ConceptStatus.IN_PROGRESS)
                        .build());

        progress.setAttempts(progress.getAttempts() + 1);
        if (correct) {
            progress.setCorrectAttempts(progress.getCorrectAttempts() + 1);
        }
        progress.setHintsUsed(progress.getHintsUsed() + (request.getHintsUsed() != null ? request.getHintsUsed() : 0));
        progress.setLastAccessedAt(LocalDateTime.now());
        boolean wasMastered = progress.getStatus() == ConceptStatus.MASTERED;
        progress.setStatus(ConceptStatus.IN_PROGRESS);

        // Recalculate mastery
        double mastery = masteryCalculator.calculate(progress);
        progress.setMasteryLevel(mastery);

        // Check mastery threshold — only award XP on first mastery
        boolean justMastered = false;
        if (mastery >= 0.85) {
            progress.setStatus(ConceptStatus.MASTERED);
            if (!wasMastered) {
                gamificationService.awardXP(userId, "CONCEPT_MASTERED", 50, conceptId);
                justMastered = true;
            }
        }

        // Check fast-track: mastered on first or second attempt with score ≥ 0.90
        boolean fastTracked = false;
        if (justMastered && progress.getAttempts() <= 2
                && adaptiveEngine.canFastTrack(userId, conceptId, mastery)) {
            progress.setFastTracked(true);
            fastTracked = true;
        }

        // Compute and persist frustration score
        int wrongAttempts = progress.getAttempts() - progress.getCorrectAttempts();
        long timeTaken = request.getTimeTakenSeconds() != null ? request.getTimeTakenSeconds() : 0L;
        double frustration = adaptiveEngine.calculateFrustrationScore(wrongAttempts, timeTaken, progress.getHintsUsed());
        progress.setFrustrationScore(frustration);

        // Schedule spaced repetition review — interval adjusted by user's learning pace
        LearningPace pace = learningProfileRepository.findById(userId)
                .map(UserLearningProfile::getPace)
                .orElse(LearningPace.MEDIUM);
        spacedRepetitionEngine.scheduleReview(progress, mastery, pace);

        progressRepository.save(progress);

        // Update weak area score based on latest progress
        updateWeakArea(user, question.getConcept(), userId, conceptId, mastery, progress);

        // Update course enrollment progress
        try {
            UUID courseId = question.getConcept().getTopic().getModule().getCourse().getId();
            enrollmentService.updateProgress(userId, courseId);
        } catch (Exception e) {
            log.warn("Could not update enrollment progress: {}", e.getMessage());
        }

        // Determine next action — frustration can override "reinforce" → "remediate"
        String nextAction = adaptiveEngine.determineNextAction(mastery, frustration);

        // Award XP for correct answer — only if this question hasn't been answered correctly before
        int xpEarned = 0;
        if (correct) {
            List<UserAttempt> previousAttempts = userAttemptRepository
                    .findByUserIdAndQuestionIdOrderByCreatedAtDesc(userId, question.getId());
            boolean previouslyCorrect = previousAttempts.stream()
                    .skip(1) // skip current attempt
                    .anyMatch(UserAttempt::getCorrect);
            if (!previouslyCorrect) {
                gamificationService.awardXP(userId, "CORRECT_ANSWER", 10, question.getId());
                xpEarned = 10;
            }
        }
        if (justMastered) {
            xpEarned += 50;
        }

        // Determine next concept if advancing
        UUID nextConceptId = null;
        if ("advance".equals(nextAction)) {
            nextConceptId = adaptiveEngine.determineNextConcept(userId, conceptId);
        }

        String feedbackText = aiFeedback != null ? aiFeedback
                : (correct ? "Excellent work!" : "Not quite right. Let's review this concept.");

        return AnswerResultResponse.builder()
                .attemptId(attempt.getId())
                .correct(correct)
                .score(score)
                .explanation(question.getExplanation())
                .feedback(feedbackText)
                .updatedMastery(mastery)
                .nextAction(nextAction)
                .xpEarned(xpEarned)
                .nextConceptId(nextConceptId)
                .fastTracked(fastTracked)
                .misconceptionTriggered(detectedMisconception != null)
                .misconceptionText(detectedMisconception)
                .build();
    }

    @Override
    public List<QuestionResponse> generateDiagnosticTest(UUID moduleId, UUID userId) {
        // Get all concepts in the module and pick a representative question from each
        return conceptRepository.findAll().stream()
                .filter(c -> c.getTopic().getModule().getId().equals(moduleId))
                .flatMap(c -> questionRepository.findByConceptId(c.getId()).stream().limit(1))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private boolean validateAnswer(Question question, Map<String, Object> answer) {
        String correctAnswer = question.getCorrectAnswer();
        if (correctAnswer == null || correctAnswer.isEmpty()) {
            Map<String, Object> metadata = question.getMetadata();
            if (metadata != null && metadata.containsKey("correctAnswer")) {
                correctAnswer = metadata.get("correctAnswer").toString();
            }
        }

        Object userAnswer = answer.get("answer");
        if (correctAnswer == null || userAnswer == null) {
            return false;
        }

        return correctAnswer.trim().equalsIgnoreCase(userAnswer.toString().trim());
    }

    private AIEvaluation evaluateWithAI(Question question, Map<String, Object> answer) {
        Object userAnswer = answer.get("answer");
        if (userAnswer == null || userAnswer.toString().isBlank()) {
            return new AIEvaluation(false, 0.0, "No answer was provided.");
        }

        String questionType = question.getType() == QuestionType.CODING ? "CODING" : "SUBJECTIVE";
        String correctAnswer = question.getCorrectAnswer() != null ? question.getCorrectAnswer() : "";

        String systemPrompt = """
            You are an expert educational assessment evaluator. Evaluate the student's answer accurately and fairly.

            RULES:
            - For SUBJECTIVE answers: check if the student demonstrates understanding of the key concepts. \
            Don't require exact wording — assess semantic correctness.
            - For CODING answers: check logical correctness, not exact syntax. The code doesn't need to compile \
            perfectly — focus on whether the approach and logic are correct.
            - Be encouraging but honest. Give specific feedback on what was right and what could be improved.
            - Award partial credit when the student shows partial understanding.

            Return ONLY valid JSON (no markdown fences) in this exact format:
            {"correct": true/false, "score": 0.0-1.0, "feedback": "Your specific feedback here"}

            Score guidelines:
            - 1.0 = Fully correct
            - 0.7-0.9 = Mostly correct with minor issues
            - 0.4-0.6 = Partially correct, shows some understanding
            - 0.1-0.3 = Mostly incorrect but shows effort
            - 0.0 = Completely wrong or irrelevant
            - "correct" should be true if score >= 0.7
            """;

        String userPrompt = String.format("""
            Question Type: %s
            Question: %s
            Reference Answer: %s
            Student's Answer: %s

            Evaluate the student's answer and return JSON.""",
                questionType, question.getQuestionText(), correctAnswer, userAnswer.toString());

        try {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4O)
                    .addSystemMessage(systemPrompt)
                    .addUserMessage(userPrompt)
                    .temperature(0.3)
                    .maxCompletionTokens(512)
                    .build();

            ChatCompletion completion = openAIClient.chat().completions().create(params);

            String response = completion.choices().stream()
                    .findFirst()
                    .map(choice -> choice.message().content().orElse(""))
                    .orElse("");

            // Strip markdown fences if present
            String cleanJson = response.strip();
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.replaceAll("^```\\w*\\n?", "").replaceAll("\\n?```$", "").strip();
            }

            JsonNode node = objectMapper.readTree(cleanJson);
            boolean correct = node.path("correct").asBoolean(false);
            double score = node.path("score").asDouble(0.0);
            String feedback = node.path("feedback").asText("Answer evaluated by AI.");

            return new AIEvaluation(correct, score, feedback);

        } catch (Exception e) {
            log.error("AI evaluation failed, falling back to keyword matching: {}", e.getMessage());
            // Fallback: keyword-based matching
            return fallbackEvaluation(question, userAnswer.toString());
        }
    }

    private AIEvaluation fallbackEvaluation(Question question, String userAnswer) {
        String correctAnswer = question.getCorrectAnswer();
        if (correctAnswer == null) {
            return new AIEvaluation(false, 0.0, "Unable to evaluate this answer.");
        }

        String userStr = userAnswer.trim().toLowerCase();
        String correctStr = correctAnswer.trim().toLowerCase();

        String[] keywords = correctStr.split("\\s+");
        long matched = 0;
        for (String kw : keywords) {
            if (kw.length() >= 3 && userStr.contains(kw)) {
                matched++;
            }
        }
        long significant = java.util.Arrays.stream(keywords).filter(k -> k.length() >= 3).count();
        double score = significant > 0 ? (double) matched / significant : 0.0;
        boolean correct = score >= 0.6;

        String feedback = correct
                ? "Your answer covers the key concepts."
                : "Your answer is missing some important points. Review the explanation for more detail.";

        return new AIEvaluation(correct, score, feedback);
    }

    private QuestionResponse mapToResponse(Question question) {
        // Remove correct answer from metadata for client
        Map<String, Object> clientMetadata = question.getMetadata() != null ?
                question.getMetadata().entrySet().stream()
                        .filter(e -> !e.getKey().equals("correctAnswer"))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                : null;

        return QuestionResponse.builder()
                .id(question.getId())
                .conceptId(question.getConcept().getId())
                .type(question.getType())
                .questionText(question.getQuestionText())
                .metadata(clientMetadata)
                .difficulty(question.getDifficulty())
                .aiGenerated(question.getAiGenerated())
                .build();
    }

    private void updateWeakArea(User user, Concept concept, UUID userId, UUID conceptId,
                                double mastery, UserConceptProgress progress) {
        if (mastery >= 0.85) {
            // Concept is mastered — remove from weak areas
            weakAreaRepository.findByUserIdAndConceptId(userId, conceptId)
                    .ifPresent(weakAreaRepository::delete);
        } else if (progress.getAttempts() > 0) {
            double score = calculateWeaknessScore(progress);
            UserWeakArea weakArea = weakAreaRepository
                    .findByUserIdAndConceptId(userId, conceptId)
                    .orElseGet(() -> UserWeakArea.builder().user(user).concept(concept).build());
            weakArea.setWeaknessScore(score);
            weakAreaRepository.save(weakArea);
        }
    }

    private double calculateWeaknessScore(UserConceptProgress progress) {
        // 60% weight on low mastery, 30% on error rate, 10% on hint reliance
        double masteryComponent = 1.0 - progress.getMasteryLevel();
        int wrong = progress.getAttempts() - progress.getCorrectAttempts();
        double errorRate = progress.getAttempts() > 0 ? (double) wrong / progress.getAttempts() : 0.0;
        double hintsComponent = Math.min(1.0, progress.getHintsUsed() * 0.1);
        return Math.min(1.0, masteryComponent * 0.6 + errorRate * 0.3 + hintsComponent * 0.1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressResponse> getReviewQueue(UUID userId) {
        List<UserConceptProgress> dueForReview = progressRepository
                .findDueForReview(userId, LocalDateTime.now());

        return dueForReview.stream()
                .map(p -> UserProgressResponse.builder()
                        .userId(userId)
                        .courseId(p.getConcept().getTopic().getModule().getCourse().getId())
                        .conceptId(p.getConcept().getId())
                        .conceptTitle(p.getConcept().getTitle())
                        .masteryLevel(p.getMasteryLevel())
                        .confidenceScore(p.getConfidenceScore())
                        .attempts(p.getAttempts())
                        .status(p.getStatus())
                        .fastTracked(p.getFastTracked())
                        .nextReviewAt(p.getNextReviewAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<QuestionResponse> generateAIQuestions(UUID conceptId, UUID userId) {
        Concept concept = conceptRepository.findById(conceptId)
                .orElseThrow(() -> new ResourceNotFoundException("Concept", "id", conceptId));

        UserConceptProgress progress = progressRepository
                .findByUserIdAndConceptId(userId, conceptId).orElse(null);

        List<Question> generated = questionGeneratorEngine.generateQuestions(concept, progress, 3);
        generated.forEach(q -> q.setGeneratedForUserId(userId));

        List<Question> saved = questionRepository.saveAll(generated);
        log.info("Generated and saved {} AI questions for concept {} for user {}", saved.size(), conceptId, userId);

        return saved.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
