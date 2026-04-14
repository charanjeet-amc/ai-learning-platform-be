package com.ailearning.platform.ai;

import com.ailearning.platform.entity.UserConceptProgress;
import com.ailearning.platform.entity.enums.ConceptStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * SM-2 based spaced repetition scheduler.
 * Schedules concept reviews at increasing intervals based on recall quality.
 *
 * Quality scale (0-5):
 *   5 = perfect response
 *   4 = correct after hesitation
 *   3 = correct with difficulty
 *   2 = incorrect but close
 *   1 = incorrect, remembered on seeing answer
 *   0 = complete blackout
 */
@Component
public class SpacedRepetitionEngine {

    private static final double MIN_EASE_FACTOR = 1.3;
    private static final int MAX_INTERVAL_DAYS = 180;

    /**
     * Convert mastery score (0-1) to SM-2 quality (0-5).
     */
    public int masteryToQuality(double mastery) {
        if (mastery >= 0.95) return 5;
        if (mastery >= 0.85) return 4;
        if (mastery >= 0.70) return 3;
        if (mastery >= 0.50) return 2;
        if (mastery >= 0.30) return 1;
        return 0;
    }

    /**
     * Schedule next review based on current mastery after an assessment attempt.
     * Modifies the progress entity in place.
     */
    public void scheduleReview(UserConceptProgress progress, double mastery) {
        int quality = masteryToQuality(mastery);
        double ef = progress.getEaseFactor() != null ? progress.getEaseFactor() : 2.5;
        int interval = progress.getReviewIntervalDays() != null ? progress.getReviewIntervalDays() : 1;

        if (quality >= 3) {
            // Successful recall — increase interval
            if (interval == 1) {
                interval = 1;
            } else if (interval <= 2) {
                interval = 6;
            } else {
                interval = (int) Math.round(interval * ef);
            }

            // Update ease factor: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
            ef = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
            ef = Math.max(MIN_EASE_FACTOR, ef);
        } else {
            // Failed recall — reset interval
            interval = 1;
            // Don't penalize ease factor too harshly
            ef = Math.max(MIN_EASE_FACTOR, ef - 0.2);
        }

        interval = Math.min(interval, MAX_INTERVAL_DAYS);

        progress.setReviewIntervalDays(interval);
        progress.setEaseFactor(ef);
        progress.setNextReviewAt(LocalDateTime.now().plusDays(interval));
    }

    /**
     * Check if a concept is due for review.
     */
    public boolean isDueForReview(UserConceptProgress progress) {
        if (progress.getNextReviewAt() == null) return false;
        if (progress.getStatus() != ConceptStatus.MASTERED) return false;
        return LocalDateTime.now().isAfter(progress.getNextReviewAt());
    }
}
