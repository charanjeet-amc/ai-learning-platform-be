package com.ailearning.platform.ai;

import com.ailearning.platform.entity.Concept;
import com.ailearning.platform.entity.UserConceptProgress;
import com.ailearning.platform.entity.enums.ConceptStatus;
import com.ailearning.platform.repository.ConceptRepository;
import com.ailearning.platform.repository.UserConceptProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdaptiveEngine {

    private static final double MASTERY_THRESHOLD = 0.85;
    private static final double REINFORCE_THRESHOLD = 0.6;
    private static final double FAST_TRACK_THRESHOLD = 0.90;
    // Raw frustration score above this triggers early remediation (≈4 wrong attempts, or 3 wrong + heavy hint use)
    private static final double FRUSTRATION_REMEDIATE_THRESHOLD = 2.0;

    private final ConceptRepository conceptRepository;
    private final UserConceptProgressRepository progressRepository;
    private final MasteryCalculator masteryCalculator;

    public String determineNextAction(double mastery, double frustrationScore) {
        if (mastery >= MASTERY_THRESHOLD) {
            return "advance";
        }
        // Student is frustrated and not mastered — skip reinforce, go straight to remediation
        if (frustrationScore >= FRUSTRATION_REMEDIATE_THRESHOLD) {
            return "remediate";
        }
        if (mastery >= REINFORCE_THRESHOLD) {
            return "reinforce";
        }
        return "remediate";
    }

    public UUID determineNextConcept(UUID userId, UUID currentConceptId) {
        Concept current = conceptRepository.findById(currentConceptId).orElse(null);
        if (current == null) return null;

        Optional<UserConceptProgress> progress =
            progressRepository.findByUserIdAndConceptId(userId, currentConceptId);

        if (progress.isPresent()) {
            double mastery = masteryCalculator.calculate(progress.get());

            // If mastery is too low, redirect to weakest prerequisite
            if (mastery < REINFORCE_THRESHOLD) {
                UUID prerequisite = findWeakestPrerequisite(userId, currentConceptId);
                if (prerequisite != null) return prerequisite;
            }
        }

        // Find next concept in sequence
        List<Concept> siblings = conceptRepository
                .findByTopicIdOrderByOrderIndexAsc(current.getTopic().getId());

        boolean foundCurrent = false;
        for (Concept sibling : siblings) {
            if (foundCurrent) return sibling.getId();
            if (sibling.getId().equals(currentConceptId)) foundCurrent = true;
        }

        // If at end of topic, go to next topic's first concept
        return null; // Controller will handle module/topic transition
    }

    public boolean canFastTrack(UUID userId, UUID conceptId, double diagnosticScore) {
        return diagnosticScore >= FAST_TRACK_THRESHOLD;
    }

    public double calculateFrustrationScore(int wrongAttempts, long idleTimeSeconds, int hintsUsed) {
        return (wrongAttempts * 0.5) + (idleTimeSeconds / 60.0 * 0.3) + (hintsUsed * 0.2);
    }

    private UUID findWeakestPrerequisite(UUID userId, UUID conceptId) {
        List<Concept> prerequisites = conceptRepository.findPrerequisites(conceptId);

        UUID weakest = null;
        double lowestMastery = Double.MAX_VALUE;

        for (Concept prereq : prerequisites) {
            Optional<UserConceptProgress> prereqProgress =
                    progressRepository.findByUserIdAndConceptId(userId, prereq.getId());

            double mastery = prereqProgress
                    .map(masteryCalculator::calculate)
                    .orElse(0.0);

            if (mastery < lowestMastery) {
                lowestMastery = mastery;
                weakest = prereq.getId();
            }
        }

        return (lowestMastery < MASTERY_THRESHOLD) ? weakest : null;
    }
}
