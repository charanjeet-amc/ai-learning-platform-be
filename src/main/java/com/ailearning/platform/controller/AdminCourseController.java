package com.ailearning.platform.controller;

import com.ailearning.platform.dto.response.CourseResponse;
import com.ailearning.platform.entity.Course;
import com.ailearning.platform.entity.enums.CourseStatus;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.repository.CourseRepository;
import com.ailearning.platform.service.CourseService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseRepository courseRepository;
    private final CourseService courseService;

    @GetMapping("/pending")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CourseResponse>> getPendingCourses() {
        List<Course> courses = courseRepository.findByStatus(CourseStatus.PENDING_APPROVAL);
        return ResponseEntity.ok(courses.stream().map(this::mapCourseResponse).toList());
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<CourseResponse>> getAllCourses(
            @RequestParam(required = false) String status) {
        List<Course> courses;
        if (status != null && !status.isBlank()) {
            courses = courseRepository.findByStatus(CourseStatus.valueOf(status));
        } else {
            courses = courseRepository.findAll();
        }
        return ResponseEntity.ok(courses.stream().map(this::mapCourseResponse).toList());
    }

    @PostMapping("/{courseId}/approve")
    @Transactional
    public ResponseEntity<Void> approveCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        course.setStatus(CourseStatus.PUBLISHED);
        course.setPublished(true);
        course.setAdminFeedback(null);
        courseRepository.save(course);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{courseId}/reject")
    @Transactional
    public ResponseEntity<Void> rejectCourse(
            @PathVariable UUID courseId,
            @RequestBody(required = false) RejectRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        course.setStatus(CourseStatus.CHANGES_REQUESTED);
        course.setPublished(false);
        course.setAdminFeedback(request != null ? request.getFeedback() : null);
        courseRepository.save(course);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{courseId}/unpublish")
    @Transactional
    public ResponseEntity<Void> unpublishCourse(@PathVariable UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        course.setStatus(CourseStatus.DRAFT);
        course.setPublished(false);
        courseRepository.save(course);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteCourse(courseId, null);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class RejectRequest {
        private String feedback;
    }

    private CourseResponse mapCourseResponse(Course course) {
        String createdByName = null;
        try {
            if (course.getCreatedBy() != null) {
                createdByName = course.getCreatedBy().getFullName();
            }
        } catch (Exception e) {
            // Lazy loading outside transaction
        }
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .shortDescription(course.getShortDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .difficulty(course.getDifficulty())
                .estimatedDurationMinutes(course.getEstimatedDurationMinutes())
                .industryVertical(course.getIndustryVertical())
                .skillsOutcome(course.getSkillsOutcome())
                .prerequisites(course.getPrerequisites())
                .tags(course.getTags())
                .category(course.getCategory())
                .published(course.getPublished())
                .status(course.getStatus() != null ? course.getStatus().name() : "DRAFT")
                .adminFeedback(course.getAdminFeedback())
                .rating(course.getRating())
                .enrollmentCount(course.getEnrollmentCount())
                .price(course.getPrice())
                .createdByName(createdByName)
                .createdAt(course.getCreatedAt())
                .build();
    }
}
