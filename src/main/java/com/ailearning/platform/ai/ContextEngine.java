package com.ailearning.platform.ai;

import com.ailearning.platform.entity.*;
import com.ailearning.platform.entity.enums.LearningStyle;
import com.ailearning.platform.repository.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContextEngine {

    private final ConceptRepository conceptRepository;
    private final LearningUnitRepository learningUnitRepository;
    private final UserConceptProgressRepository progressRepository;
    private final UserLearningProfileRepository profileRepository;
    private final AIInteractionRepository interactionRepository;

    @Data
    @Builder
    public static class TutorContext {
        private Concept concept;
        private List<LearningUnit> learningUnits;
        private List<Concept> prerequisites;
        private List<String> misconceptions;
        private List<String> socraticQuestions;
        private UserConceptProgress userProgress;
        private LearningStyle learningStyle;
        private List<AIInteraction> recentInteractions;
        private String userLevel;
    }

    public TutorContext resolveContext(UUID userId, UUID conceptId, UUID courseId) {
        Concept concept = conceptRepository.findById(conceptId).orElse(null);
        if (concept == null) return null;

        List<LearningUnit> units = learningUnitRepository
                .findByConceptIdOrderByOrderIndexAsc(conceptId);

        List<Concept> prerequisites = conceptRepository.findPrerequisites(conceptId);

        List<String> misconceptions = concept.getMisconceptions().stream()
                .map(ConceptMisconception::getMisconception)
                .collect(Collectors.toList());

        List<String> socraticQuestions = concept.getSocraticQuestions().stream()
                .map(ConceptSocratic::getQuestion)
                .collect(Collectors.toList());

        UserConceptProgress progress = progressRepository
                .findByUserIdAndConceptId(userId, conceptId).orElse(null);

        UserLearningProfile profile = profileRepository.findById(userId).orElse(null);
        LearningStyle style = profile != null ? profile.getPreferredStyle() : LearningStyle.TEXT;

        List<AIInteraction> recentInteractions = interactionRepository
                .findByUserIdAndConceptIdOrderByCreatedAtDesc(userId, conceptId, PageRequest.of(0, 5));

        String userLevel = determineUserLevel(progress);

        return TutorContext.builder()
                .concept(concept)
                .learningUnits(units)
                .prerequisites(prerequisites)
                .misconceptions(misconceptions)
                .socraticQuestions(socraticQuestions)
                .userProgress(progress)
                .learningStyle(style)
                .recentInteractions(recentInteractions)
                .userLevel(userLevel)
                .build();
    }

    private String determineUserLevel(UserConceptProgress progress) {
        if (progress == null) return "beginner";
        double mastery = progress.getMasteryLevel();
        if (mastery >= 0.85) return "advanced";
        if (mastery >= 0.5) return "intermediate";
        return "beginner";
    }
}
