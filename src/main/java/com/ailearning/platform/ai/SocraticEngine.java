package com.ailearning.platform.ai;

import com.ailearning.platform.entity.AIInteraction;
import com.ailearning.platform.entity.Concept;
import com.ailearning.platform.entity.LearningUnit;
import com.ailearning.platform.entity.enums.LearningStyle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SocraticEngine {

    public String buildSystemPrompt(ContextEngine.TutorContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are a Socratic AI tutor for an adaptive learning platform. ");
        sb.append("Your role is to guide learners to understanding through questions and hints, ");
        sb.append("NOT by providing direct answers.\n\n");

        sb.append("## RULES:\n");
        sb.append("1. NEVER give the direct answer immediately\n");
        sb.append("2. Ask guiding questions to help the student think\n");
        sb.append("3. Provide tiered hints when the student is stuck\n");
        sb.append("4. Adapt your explanation style based on the student's level\n");
        sb.append("5. Stay within the scope of the current concept and its prerequisites\n");
        sb.append("6. Use examples, analogies, and visual descriptions when helpful\n");
        sb.append("7. If the student shows frustration (multiple wrong attempts), be more supportive\n\n");

        sb.append("## HINT ESCALATION:\n");
        sb.append("- Level 1: Ask a guiding question\n");
        sb.append("- Level 2: Give a visual/conceptual hint\n");
        sb.append("- Level 3: Provide step-by-step guidance\n");
        sb.append("- Level 4: Give the full explanation (only when frustrated)\n\n");

        return sb.toString();
    }

    public String buildContextPrompt(ContextEngine.TutorContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append("## CURRENT CONTEXT:\n\n");

        if (context.getConcept() != null) {
            Concept concept = context.getConcept();
            sb.append("### Concept: ").append(concept.getTitle()).append("\n");
            sb.append("Definition: ").append(concept.getDefinition()).append("\n");
            sb.append("Difficulty: ").append(concept.getDifficultyLevel()).append("\n\n");
        }

        if (!context.getPrerequisites().isEmpty()) {
            sb.append("### Prerequisites:\n");
            context.getPrerequisites().forEach(p ->
                    sb.append("- ").append(p.getTitle()).append(": ").append(p.getDefinition()).append("\n"));
            sb.append("\n");
        }

        if (!context.getMisconceptions().isEmpty()) {
            sb.append("### Common Misconceptions (watch for these):\n");
            context.getMisconceptions().forEach(m -> sb.append("- ").append(m).append("\n"));
            sb.append("\n");
        }

        if (!context.getSocraticQuestions().isEmpty()) {
            sb.append("### Suggested Socratic Questions:\n");
            context.getSocraticQuestions().forEach(q -> sb.append("- ").append(q).append("\n"));
            sb.append("\n");
        }

        // Learning units content
        if (!context.getLearningUnits().isEmpty()) {
            sb.append("### Learning Material:\n");
            for (LearningUnit unit : context.getLearningUnits()) {
                if (unit.getContent() != null && unit.getContent().containsKey("text")) {
                    sb.append(unit.getContent().get("text")).append("\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public String buildUserStatePrompt(ContextEngine.TutorContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append("## STUDENT STATE:\n");
        sb.append("Level: ").append(context.getUserLevel()).append("\n");
        sb.append("Preferred Style: ").append(context.getLearningStyle()).append("\n");

        if (context.getUserProgress() != null) {
            sb.append("Mastery: ").append(String.format("%.1f%%", context.getUserProgress().getMasteryLevel() * 100)).append("\n");
            sb.append("Attempts: ").append(context.getUserProgress().getAttempts()).append("\n");
            sb.append("Hints Used: ").append(context.getUserProgress().getHintsUsed()).append("\n");
        }

        // Style-specific instructions
        sb.append("\n## STYLE INSTRUCTIONS:\n");
        switch (context.getLearningStyle()) {
            case VISUAL -> sb.append("Use diagrams, flowcharts, and visual descriptions. Describe things spatially.\n");
            case CODE -> sb.append("Use code examples, pseudocode, and programming analogies.\n");
            case AUDITORY -> sb.append("Use conversational tone, stories, and verbal analogies.\n");
            default -> sb.append("Use clear text explanations with examples and analogies.\n");
        }

        // Include recent interaction history for context
        if (!context.getRecentInteractions().isEmpty()) {
            sb.append("\n## RECENT CONVERSATION:\n");
            for (AIInteraction interaction : context.getRecentInteractions()) {
                sb.append("Student: ").append(interaction.getQuery()).append("\n");
                sb.append("You: ").append(interaction.getResponse()).append("\n\n");
            }
        }

        return sb.toString();
    }

    public int determineHintLevel(ContextEngine.TutorContext context) {
        if (context.getUserProgress() == null) return 1;

        int attempts = context.getUserProgress().getAttempts();
        int hintsUsed = context.getUserProgress().getHintsUsed();

        if (attempts >= 3 && hintsUsed >= 3) return 4; // Frustration - give answer
        if (attempts >= 2) return 3; // Step-by-step
        if (attempts >= 1) return 2; // Stronger hint
        return 1; // Guiding question
    }
}
