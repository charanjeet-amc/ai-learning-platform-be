package com.ailearning.platform.controller;

import com.ailearning.platform.dto.request.CreateCourseRequest;
import com.ailearning.platform.dto.response.CourseProgressResponse;
import com.ailearning.platform.dto.response.CourseResponse;
import com.ailearning.platform.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/courses")
    public ResponseEntity<Page<CourseResponse>> listCourses(
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(courseService.listCourses(pageable));
    }

    @GetMapping("/courses/search")
    public ResponseEntity<Page<CourseResponse>> searchCourses(
            @RequestParam String q,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(courseService.searchCourses(q, pageable));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.getCourse(courseId));
    }

    @GetMapping("/courses/{courseId}/tree")
    public ResponseEntity<CourseResponse> getCourseWithTree(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.getCourseWithTree(courseId));
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.createCourse(request, userId));
    }

    @PutMapping("/courses/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(courseService.updateCourse(courseId, request, userId));
    }

    @PostMapping("/courses/{courseId}/publish")
    public ResponseEntity<Void> publishCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        courseService.publishCourse(courseId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/courses/{courseId}/progress")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(courseService.getCourseProgress(courseId, userId));
    }

    private UUID extractUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
