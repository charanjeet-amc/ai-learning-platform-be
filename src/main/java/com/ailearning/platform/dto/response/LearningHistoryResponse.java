package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LearningHistoryResponse {
    private Long totalXp;
    private Integer totalCoursesEnrolled;
    private Integer totalCoursesCompleted;
    private Integer totalConceptsMastered;
    private Integer totalQuestionsAnswered;
    private Integer totalCorrectAnswers;
    private List<CourseHistoryEntry> courses;
    private List<RecentActivityEntry> recentActivity;

    @Data
    @Builder
    public static class CourseHistoryEntry {
        private UUID courseId;
        private String courseTitle;
        private String thumbnailUrl;
        private Double progressPercent;
        private Boolean completed;
        private LocalDateTime enrolledAt;
        private LocalDateTime completedAt;
        private Integer conceptsTotal;
        private Integer conceptsMastered;
        private Integer conceptsInProgress;
        private Integer questionsAttempted;
    }

    @Data
    @Builder
    public static class RecentActivityEntry {
        private String type; // ENROLLMENT, QUIZ_ATTEMPT, CONCEPT_MASTERED, COURSE_COMPLETED
        private String description;
        private LocalDateTime timestamp;
        private UUID referenceId;
    }
}
