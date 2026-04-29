package com.ailearning.platform.service.impl;

import com.ailearning.platform.ai.AdaptiveEngine;
import com.ailearning.platform.ai.ContextEngine;
import com.ailearning.platform.ai.MasteryCalculator;
import com.ailearning.platform.ai.SocraticEngine;
import com.ailearning.platform.dto.request.AITutorRequest;
import com.ailearning.platform.dto.response.AITutorResponse;
import com.ailearning.platform.entity.AIInteraction;
import com.ailearning.platform.entity.User;
import com.ailearning.platform.entity.UserConceptProgress;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.repository.AIInteractionRepository;
import com.ailearning.platform.repository.UserConceptProgressRepository;
import com.ailearning.platform.repository.UserRepository;
import com.ailearning.platform.service.AITutorService;
import com.ailearning.platform.service.GamificationService;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AITutorServiceImpl implements AITutorService {

    private final OpenAIClient openAIClient;
    private final ContextEngine contextEngine;
    private final SocraticEngine socraticEngine;
    private final AdaptiveEngine adaptiveEngine;
    private final MasteryCalculator masteryCalculator;
    private final AIInteractionRepository interactionRepository;
    private final UserConceptProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    @Override
    @Transactional
    public AITutorResponse chat(AITutorRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 1. Resolve context
        ContextEngine.TutorContext context = contextEngine.resolveContext(
                userId, request.getConceptId(), request.getCourseId());

        if (context == null) {
            return AITutorResponse.builder()
                    .message("I couldn't find the concept you're asking about. Please make sure you're on a valid lesson.")
                    .responseType("error")
                    .build();
        }

        // 2. Build layered prompts
        String systemPrompt = socraticEngine.buildSystemPrompt(context);
        String contextPrompt = socraticEngine.buildContextPrompt(context);
        String userStatePrompt = socraticEngine.buildUserStatePrompt(context);
        int hintLevel = socraticEngine.determineHintLevel(context);

        // 3. Call OpenAI
        String fullSystemPrompt = systemPrompt + contextPrompt + userStatePrompt
                + "\nCurrent hint level: " + hintLevel + "/4\n";

        String aiResponse = callOpenAI(fullSystemPrompt, request.getQuery());

        // 4. Determine response type based on hint level
        String responseType = switch (hintLevel) {
            case 1 -> "question";
            case 2 -> "hint";
            case 3 -> "step_by_step";
            case 4 -> "explanation";
            default -> "explanation";
        };

        // 5. Determine next action
        String nextAction = null;
        if (context.getUserProgress() != null) {
            double mastery = masteryCalculator.calculate(context.getUserProgress());
            double frustration = context.getUserProgress().getFrustrationScore() != null
                    ? context.getUserProgress().getFrustrationScore() : 0.0;
            nextAction = adaptiveEngine.determineNextAction(mastery, frustration);
        }

        // 6. Save interaction
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        AIInteraction interaction = AIInteraction.builder()
                .user(user)
                .concept(context.getConcept())
                .query(request.getQuery())
                .response(aiResponse)
                .sessionId(sessionId)
                .build();
        interactionRepository.save(interaction);

        // 7. Award XP for engaging with AI tutor
        gamificationService.awardXP(userId, "AI_TUTOR_INTERACTION", 2, request.getConceptId());

        return AITutorResponse.builder()
                .message(aiResponse)
                .sessionId(sessionId)
                .responseType(responseType)
                .hintLevel(hintLevel)
                .suggestedAction(nextAction)
                .conceptId(request.getConceptId() != null ? request.getConceptId().toString() : null)
                .build();
    }

    private String callOpenAI(String systemPrompt, String userMessage) {
        try {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4O)
                    .addSystemMessage(systemPrompt)
                    .addUserMessage(userMessage)
                    .temperature(0.7)
                    .maxCompletionTokens(1024)
                    .build();

            ChatCompletion completion = openAIClient.chat().completions().create(params);

            return completion.choices().stream()
                    .findFirst()
                    .map(choice -> choice.message().content().orElse("I'm having trouble generating a response."))
                    .orElse("I'm having trouble generating a response.");

        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            return "I'm temporarily unavailable. Please try again in a moment.";
        }
    }
}
