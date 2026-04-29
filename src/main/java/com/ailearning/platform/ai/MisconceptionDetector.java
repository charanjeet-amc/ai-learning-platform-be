package com.ailearning.platform.ai;

import com.ailearning.platform.entity.ConceptMisconception;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Detects whether a wrong answer indicates a known misconception.
 * Extracts domain keywords (≥5 chars, non-stopword) from each misconception text
 * and checks if any appear in the user's answer. The first matching misconception
 * text is returned, or null if none match.
 */
@Component
public class MisconceptionDetector {

    // Words that appear in misconception descriptions but carry no domain signal
    private static final Set<String> STOPWORDS = Set.of(
        "thinking", "confusing", "confuse", "believing", "believe",
        "misunderstanding", "assuming", "assume", "students", "learners",
        "often", "always", "commonly", "incorrectly", "their", "there",
        "which", "would", "about", "after", "other", "these", "those",
        "when", "that", "with", "from", "have", "will", "been", "than",
        "more", "also", "into", "over", "such", "then", "them", "some",
        "what", "your", "because", "should", "could", "might"
    );

    /**
     * Returns the text of the first misconception whose keywords appear in the
     * user's wrong answer, or null if no misconception is detected.
     */
    public String detect(String userAnswer, List<ConceptMisconception> misconceptions) {
        if (userAnswer == null || userAnswer.isBlank()
                || misconceptions == null || misconceptions.isEmpty()) {
            return null;
        }
        String answerLower = userAnswer.toLowerCase();
        for (ConceptMisconception m : misconceptions) {
            if (matches(answerLower, m.getMisconception())) {
                return m.getMisconception();
            }
        }
        return null;
    }

    private boolean matches(String answerLower, String misconception) {
        if (misconception == null || misconception.isBlank()) return false;
        String[] tokens = misconception.toLowerCase().split("[\\s\\p{Punct}]+");
        return java.util.Arrays.stream(tokens)
                .filter(w -> w.length() >= 5 && !STOPWORDS.contains(w))
                .anyMatch(answerLower::contains);
    }
}
