package com.ailearning.platform.ai;

import com.ailearning.platform.entity.Concept;
import com.ailearning.platform.entity.Question;
import com.ailearning.platform.entity.UserConceptProgress;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import com.ailearning.platform.entity.enums.QuestionType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates assessment questions dynamically using GPT-4o.
 * Produces MCQ questions at appropriate difficulty levels for a concept.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionGeneratorEngine {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
        You are an expert educational assessment creator. Generate high-quality multiple-choice questions
        for adaptive learning assessments.

        RULES:
        - Each question must test understanding, not just memorization
        - Include plausible distractors (wrong answers that reflect common misconceptions)
        - Provide clear, educational explanations for the correct answer
        - Match the specified difficulty level precisely
        - Return ONLY valid JSON, no markdown formatting

        DIFFICULTY GUIDELINES:
        - BEGINNER: Test basic recall and recognition
        - EASY: Test understanding and simple application
        - MEDIUM: Test application and analysis
        - HARD: Test analysis and evaluation
        - ADVANCED: Test synthesis and creation, multi-step reasoning
        """;

    /**
     * Generate questions for a concept at an appropriate difficulty level.
     */
    public List<Question> generateQuestions(Concept concept, UserConceptProgress progress, int count) {
        DifficultyLevel targetDifficulty = determineTargetDifficulty(progress);
        String userPrompt = buildGenerationPrompt(concept, targetDifficulty, count);

        try {
            String responseJson = callOpenAI(userPrompt);
            return parseQuestions(responseJson, concept);
        } catch (Exception e) {
            log.error("Failed to generate questions for concept {}: {}", concept.getId(), e.getMessage());
            return List.of();
        }
    }

    private DifficultyLevel determineTargetDifficulty(UserConceptProgress progress) {
        if (progress == null) return DifficultyLevel.BEGINNER;
        double mastery = progress.getMasteryLevel() != null ? progress.getMasteryLevel() : 0.0;

        if (mastery >= 0.85) return DifficultyLevel.ADVANCED;
        if (mastery >= 0.70) return DifficultyLevel.HARD;
        if (mastery >= 0.50) return DifficultyLevel.MEDIUM;
        if (mastery >= 0.25) return DifficultyLevel.EASY;
        return DifficultyLevel.BEGINNER;
    }

    private String buildGenerationPrompt(Concept concept, DifficultyLevel difficulty, int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate ").append(count).append(" multiple-choice questions about:\n\n");
        sb.append("CONCEPT: ").append(concept.getTitle()).append("\n");
        if (concept.getDefinition() != null) {
            sb.append("DEFINITION: ").append(concept.getDefinition()).append("\n");
        }
        sb.append("DIFFICULTY: ").append(difficulty.name()).append("\n\n");

        // Include misconceptions if available
        if (concept.getMisconceptions() != null && !concept.getMisconceptions().isEmpty()) {
            sb.append("COMMON MISCONCEPTIONS (use as distractors):\n");
            concept.getMisconceptions().forEach(m ->
                sb.append("- ").append(m.getMisconception()).append("\n"));
            sb.append("\n");
        }

        // Include learning outcomes if available
        if (concept.getOutcomes() != null && !concept.getOutcomes().isEmpty()) {
            sb.append("LEARNING OUTCOMES TO TEST:\n");
            concept.getOutcomes().forEach(o ->
                sb.append("- ").append(o.getOutcome()).append("\n"));
            sb.append("\n");
        }

        sb.append("""
            Return a JSON array with this exact structure:
            [
              {
                "questionText": "What is...?",
                "options": ["Option A", "Option B", "Option C", "Option D"],
                "correctAnswer": "Option A",
                "explanation": "Option A is correct because...",
                "difficulty": "%s"
              }
            ]
            """.formatted(difficulty.name()));

        return sb.toString();
    }

    private String callOpenAI(String userPrompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .addSystemMessage(SYSTEM_PROMPT)
                .addUserMessage(userPrompt)
                .temperature(0.8)
                .maxCompletionTokens(2048)
                .build();

        ChatCompletion completion = openAIClient.chat().completions().create(params);

        return completion.choices().stream()
                .findFirst()
                .map(choice -> choice.message().content().orElse("[]"))
                .orElse("[]");
    }

    private List<Question> parseQuestions(String json, Concept concept) {
        List<Question> questions = new ArrayList<>();
        try {
            // Strip markdown code fences if present
            String cleanJson = json.strip();
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.replaceAll("^```\\w*\\n?", "").replaceAll("\\n?```$", "").strip();
            }

            List<Map<String, Object>> parsed = objectMapper.readValue(
                    cleanJson, new TypeReference<>() {});

            for (Map<String, Object> q : parsed) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("options", q.get("options"));
                metadata.put("correctAnswer", q.get("correctAnswer"));

                DifficultyLevel diff;
                try {
                    diff = DifficultyLevel.valueOf(String.valueOf(q.getOrDefault("difficulty", "MEDIUM")));
                } catch (IllegalArgumentException e) {
                    diff = DifficultyLevel.MEDIUM;
                }

                Question question = Question.builder()
                        .concept(concept)
                        .type(QuestionType.MCQ)
                        .questionText(String.valueOf(q.get("questionText")))
                        .metadata(metadata)
                        .difficulty(diff)
                        .explanation(String.valueOf(q.getOrDefault("explanation", "")))
                        .aiGenerated(true)
                        .build();

                questions.add(question);
            }
        } catch (Exception e) {
            log.error("Failed to parse AI-generated questions: {}", e.getMessage());
        }
        return questions;
    }
}
