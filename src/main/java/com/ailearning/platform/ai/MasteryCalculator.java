package com.ailearning.platform.ai;

import com.ailearning.platform.entity.UserConceptProgress;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Computes mastery score using the formula:
 * M = 0.4*S + 0.2*C + 0.2*R + 0.2*T
 *
 * S = Success Rate (correct/total attempts)
 * C = Confidence (1 - hintsUsed/maxHints)
 * R = Recency (e^(-λt))
 * T = Time efficiency (expectedTime/actualTime, clamped 0-1)
 */
@Component
public class MasteryCalculator {

    private static final double WEIGHT_SUCCESS = 0.4;
    private static final double WEIGHT_CONFIDENCE = 0.2;
    private static final double WEIGHT_RECENCY = 0.2;
    private static final double WEIGHT_EFFICIENCY = 0.2;
    private static final double DECAY_LAMBDA = 0.1;
    private static final int MAX_HINTS = 4;
    private static final long EXPECTED_TIME_PER_CONCEPT_SECONDS = 300; // 5 minutes

    public double calculate(UserConceptProgress progress) {
        double successRate = calculateSuccessRate(progress);
        double confidence = calculateConfidence(progress);
        double recency = calculateRecency(progress);
        double efficiency = calculateEfficiency(progress);

        double mastery = (WEIGHT_SUCCESS * successRate)
                + (WEIGHT_CONFIDENCE * confidence)
                + (WEIGHT_RECENCY * recency)
                + (WEIGHT_EFFICIENCY * efficiency);

        return Math.min(1.0, Math.max(0.0, mastery));
    }

    private double calculateSuccessRate(UserConceptProgress progress) {
        if (progress.getAttempts() == 0) return 0.0;
        return (double) progress.getCorrectAttempts() / progress.getAttempts();
    }

    private double calculateConfidence(UserConceptProgress progress) {
        int hintsUsed = Math.min(progress.getHintsUsed(), MAX_HINTS);
        return 1.0 - ((double) hintsUsed / MAX_HINTS);
    }

    private double calculateRecency(UserConceptProgress progress) {
        if (progress.getLastAccessedAt() == null) return 0.0;
        long daysSinceAccess = Duration.between(progress.getLastAccessedAt(), LocalDateTime.now()).toDays();
        return Math.exp(-DECAY_LAMBDA * daysSinceAccess);
    }

    private double calculateEfficiency(UserConceptProgress progress) {
        if (progress.getTimeSpentSeconds() == 0) return 0.5; // default
        double ratio = (double) EXPECTED_TIME_PER_CONCEPT_SECONDS / progress.getTimeSpentSeconds();
        return Math.min(1.0, Math.max(0.0, ratio));
    }
}
