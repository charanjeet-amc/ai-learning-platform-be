package com.ailearning.platform.service.impl;

import com.ailearning.platform.dto.request.SubmitAnswerRequest;
import com.ailearning.platform.dto.response.AnswerResultResponse;
import com.ailearning.platform.dto.response.QuestionResponse;
import com.ailearning.platform.entity.*;
import com.ailearning.platform.entity.enums.ConceptStatus;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.repository.*;
import com.ailearning.platform.ai.MasteryCalculator;
import com.ailearning.platform.ai.AdaptiveEngine;
import com.ailearning.platform.ai.SpacedRepetitionEngine;
import com.ailearning.platform.ai.QuestionGeneratorEngine;
import com.ailearning.platform.service.AssessmentService;
import com.ailearning.platform.service.EnrollmentService;
import com.ailearning.platform.service.GamificationService;
import com.ailearning.platform.dto.response.UserProgressResponse;
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
    private final UserRepository userRepository;
    private final ConceptRepository conceptRepository;
    private final MasteryCalculator masteryCalculator;
    private final AdaptiveEngine adaptiveEngine;
    private final SpacedRepetitionEngine spacedRepetitionEngine;
    private final QuestionGeneratorEngine questionGeneratorEngine;
    private final GamificationService gamificationService;
    private final EnrollmentService enrollmentService;

    @Override
    public List<QuestionResponse> getQuestionsForConcept(UUID conceptId, UUID userId) {
        UserConceptProgress progress = progressRepository
                .findByUserIdAndConceptId(userId, conceptId).orElse(null);

        List<Question> questions = questionRepository.findByConceptId(conceptId);

        // Filter by difficulty based on user progress
        if (progress != null && progress.getMasteryLevel() > 0.6) {
            questions = questions.stream()
                    .filter(q -> q.getDifficulty().ordinal() >= progress.getMasteryLevel() * 4)
                    .collect(Collectors.toList());
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
        boolean correct = validateAnswer(question, request.getAnswer());
        double score = correct ? 1.0 : 0.0;

        // Record the attempt
        UserAttempt attempt = UserAttempt.builder()
                .user(user)
                .question(question)
                .answer(request.getAnswer())
                .score(score)
                .correct(correct)
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .hintsUsed(request.getHintsUsed() != null ? request.getHintsUsed() : 0)
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
        progress.setStatus(ConceptStatus.IN_PROGRESS);

        // Recalculate mastery
        double mastery = masteryCalculator.calculate(progress);
        progress.setMasteryLevel(mastery);

        // Check mastery threshold — only award XP on first mastery
        boolean justMastered = false;
        if (mastery >= 0.85 && progress.getStatus() != ConceptStatus.MASTERED) {
            progress.setStatus(ConceptStatus.MASTERED);
            gamificationService.awardXP(userId, "CONCEPT_MASTERED", 50, conceptId);
            justMastered = true;
        } else if (mastery >= 0.85) {
            progress.setStatus(ConceptStatus.MASTERED);
        }

        // Schedule spaced repetition review
        spacedRepetitionEngine.scheduleReview(progress, mastery);

        progressRepository.save(progress);

        // Update course enrollment progress
        try {
            UUID courseId = question.getConcept().getTopic().getModule().getCourse().getId();
            enrollmentService.updateProgress(userId, courseId);
        } catch (Exception e) {
            log.warn("Could not update enrollment progress: {}", e.getMessage());
        }

        // Determine next action
        String nextAction = adaptiveEngine.determineNextAction(mastery);

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

        return AnswerResultResponse.builder()
                .attemptId(attempt.getId())
                .correct(correct)
                .score(score)
                .explanation(question.getExplanation())
                .feedback(correct ? "Excellent work!" : "Not quite right. Let's review this concept.")
                .updatedMastery(mastery)
                .nextAction(nextAction)
                .xpEarned(xpEarned)
                .nextConceptId(nextConceptId)
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
        // Prefer correctAnswer field on entity, fall back to metadata
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

        return correctAnswer.equalsIgnoreCase(userAnswer.toString());
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

    @Override
    public List<UserProgressResponse> getReviewQueue(UUID userId) {
        List<UserConceptProgress> dueForReview = progressRepository
                .findDueForReview(userId, LocalDateTime.now());

        return dueForReview.stream()
                .map(p -> UserProgressResponse.builder()
                        .userId(userId)
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

        // Save generated questions to DB
        List<Question> saved = questionRepository.saveAll(generated);
        log.info("Generated and saved {} AI questions for concept {}", saved.size(), conceptId);

        return saved.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
