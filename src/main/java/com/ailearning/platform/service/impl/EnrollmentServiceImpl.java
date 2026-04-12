package com.ailearning.platform.service.impl;

import com.ailearning.platform.dto.response.EnrolledCourseResponse;
import com.ailearning.platform.entity.*;
import com.ailearning.platform.entity.enums.ConceptStatus;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.repository.*;
import com.ailearning.platform.service.EnrollmentService;
import com.ailearning.platform.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserConceptProgressRepository progressRepository;
    private final GamificationService gamificationService;

    @Override
    @Transactional
    public void enroll(UUID userId, UUID courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalArgumentException("Already enrolled in this course");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .build();

        enrollmentRepository.save(enrollment);

        // Initialize progress for first concept
        course.getModules().stream()
                .flatMap(m -> m.getTopics().stream())
                .flatMap(t -> t.getConcepts().stream())
                .findFirst()
                .ifPresent(firstConcept -> {
                    UserConceptProgress progress = UserConceptProgress.builder()
                            .user(user)
                            .concept(firstConcept)
                            .status(ConceptStatus.UNLOCKED)
                            .build();
                    progressRepository.save(progress);
                    enrollment.setCurrentConceptId(firstConcept.getId());
                    enrollmentRepository.save(enrollment);
                });

        // Update enrollment count
        course.setEnrollmentCount(course.getEnrollmentCount() + 1);
        courseRepository.save(course);

        // Award XP for enrollment
        gamificationService.awardXP(userId, "ENROLLMENT", 10, courseId);
    }

    @Override
    @Transactional
    public void unenroll(UUID userId, UUID courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "userId+courseId", userId));
        enrollmentRepository.delete(enrollment);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        course.setEnrollmentCount(Math.max(0, course.getEnrollmentCount() - 1));
        courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrolledCourseResponse> getUserEnrollments(UUID userId) {
        return enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnrolled(UUID userId, UUID courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    @Transactional
    public void updateProgress(UUID userId, UUID courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "userId+courseId", userId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        long totalConcepts = course.getModules().stream()
                .flatMap(m -> m.getTopics().stream())
                .flatMap(t -> t.getConcepts().stream())
                .count();

        long mastered = progressRepository.countMasteredByUserAndCourse(userId, courseId);

        double progress = totalConcepts > 0 ? (double) mastered / totalConcepts * 100 : 0;
        enrollment.setProgressPercent(progress);

        if (progress >= 100) {
            enrollment.setCompleted(true);
            enrollment.setCompletedAt(java.time.LocalDateTime.now());
            gamificationService.awardXP(userId, "COURSE_COMPLETED", 500, courseId);
        }

        enrollmentRepository.save(enrollment);
    }

    private EnrolledCourseResponse mapToResponse(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        return EnrolledCourseResponse.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .progressPercent(enrollment.getProgressPercent())
                .completed(enrollment.getCompleted())
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }
}
