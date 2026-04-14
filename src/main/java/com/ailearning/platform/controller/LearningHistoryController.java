package com.ailearning.platform.controller;

import com.ailearning.platform.dto.response.LearningHistoryResponse;
import com.ailearning.platform.dto.response.LearningHistoryResponse.CourseHistoryEntry;
import com.ailearning.platform.dto.response.LearningHistoryResponse.RecentActivityEntry;
import com.ailearning.platform.entity.Enrollment;
import com.ailearning.platform.entity.User;
import com.ailearning.platform.entity.UserAttempt;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/learning-history")
@RequiredArgsConstructor
public class LearningHistoryController {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserConceptProgressRepository progressRepository;
    private final UserAttemptRepository attemptRepository;

    @GetMapping
    public ResponseEntity<LearningHistoryResponse> getLearningHistory(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Enrollment> enrollments = enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId);
        long totalMastered = progressRepository.countMasteredByUser(userId);
        long totalAttempts = attemptRepository.countByUserId(userId);
        long totalCorrect = attemptRepository.countByUserIdAndCorrectTrue(userId);

        // Build per-course history
        List<CourseHistoryEntry> courses = enrollments.stream().map(enrollment -> {
            UUID courseId = enrollment.getCourse().getId();
            long totalConcepts = enrollment.getCourse().getModules().stream()
                    .flatMap(m -> m.getTopics().stream())
                    .flatMap(t -> t.getConcepts().stream())
                    .count();
            long mastered = progressRepository.countMasteredByUserAndCourse(userId, courseId);
            long inProgress = progressRepository.countInProgressByUserAndCourse(userId, courseId);
            long questionsAttempted = attemptRepository.countByCourseAndUser(userId, courseId);

            return CourseHistoryEntry.builder()
                    .courseId(courseId)
                    .courseTitle(enrollment.getCourse().getTitle())
                    .thumbnailUrl(enrollment.getCourse().getThumbnailUrl())
                    .progressPercent(enrollment.getProgressPercent())
                    .completed(enrollment.getCompleted())
                    .enrolledAt(enrollment.getEnrolledAt())
                    .completedAt(enrollment.getCompletedAt())
                    .conceptsTotal((int) totalConcepts)
                    .conceptsMastered((int) mastered)
                    .conceptsInProgress((int) inProgress)
                    .questionsAttempted((int) questionsAttempted)
                    .build();
        }).collect(Collectors.toList());

        // Build recent activity from attempts (last 20)
        List<RecentActivityEntry> recentActivity = new ArrayList<>();

        // Add enrollment events
        enrollments.forEach(e -> {
            recentActivity.add(RecentActivityEntry.builder()
                    .type("ENROLLMENT")
                    .description("Enrolled in " + e.getCourse().getTitle())
                    .timestamp(e.getEnrolledAt())
                    .referenceId(e.getCourse().getId())
                    .build());
            if (Boolean.TRUE.equals(e.getCompleted()) && e.getCompletedAt() != null) {
                recentActivity.add(RecentActivityEntry.builder()
                        .type("COURSE_COMPLETED")
                        .description("Completed " + e.getCourse().getTitle())
                        .timestamp(e.getCompletedAt())
                        .referenceId(e.getCourse().getId())
                        .build());
            }
        });

        // Add recent quiz attempts
        List<UserAttempt> recentAttempts = attemptRepository.findByUserIdOrderByCreatedAtDesc(userId);
        recentAttempts.stream().limit(20).forEach(a -> {
            String conceptTitle = a.getQuestion().getConcept().getTitle();
            recentActivity.add(RecentActivityEntry.builder()
                    .type("QUIZ_ATTEMPT")
                    .description((Boolean.TRUE.equals(a.getCorrect()) ? "Correctly answered" : "Attempted")
                            + " a question in " + conceptTitle)
                    .timestamp(a.getCreatedAt())
                    .referenceId(a.getQuestion().getId())
                    .build());
        });

        // Sort by timestamp descending
        recentActivity.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        long completedCourses = enrollments.stream().filter(e -> Boolean.TRUE.equals(e.getCompleted())).count();

        return ResponseEntity.ok(LearningHistoryResponse.builder()
                .totalXp(user.getTotalXp())
                .totalCoursesEnrolled(enrollments.size())
                .totalCoursesCompleted((int) completedCourses)
                .totalConceptsMastered((int) totalMastered)
                .totalQuestionsAnswered((int) totalAttempts)
                .totalCorrectAnswers((int) totalCorrect)
                .courses(courses)
                .recentActivity(recentActivity.stream().limit(50).collect(Collectors.toList()))
                .build());
    }
}
