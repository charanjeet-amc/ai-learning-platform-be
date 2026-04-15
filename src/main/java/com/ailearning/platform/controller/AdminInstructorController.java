package com.ailearning.platform.controller;

import com.ailearning.platform.config.JwtTokenProvider;
import com.ailearning.platform.entity.InstructorApplication;
import com.ailearning.platform.entity.User;
import com.ailearning.platform.entity.enums.ApplicationStatus;
import com.ailearning.platform.entity.enums.UserRole;
import com.ailearning.platform.repository.InstructorApplicationRepository;
import com.ailearning.platform.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/instructor-applications")
@RequiredArgsConstructor
public class AdminInstructorController {

    private final InstructorApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ApplicationSummary>> listApplications(
            @RequestParam(defaultValue = "PENDING") String status) {
        ApplicationStatus appStatus;
        try {
            appStatus = ApplicationStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            appStatus = ApplicationStatus.PENDING;
        }

        List<ApplicationSummary> summaries = applicationRepository
                .findByStatusOrderByCreatedAtAsc(appStatus)
                .stream()
                .map(this::toSummary)
                .toList();

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getApplication(@PathVariable UUID id) {
        return applicationRepository.findById(id)
                .map(app -> ResponseEntity.ok(toDetail(app)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve")
    @Transactional
    public ResponseEntity<?> approve(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) ReviewRequest request) {
        InstructorApplication app = applicationRepository.findById(id).orElse(null);
        if (app == null) return ResponseEntity.notFound().build();

        UUID adminId = UUID.fromString(jwt.getSubject());
        app.setStatus(ApplicationStatus.APPROVED);
        app.setReviewedBy(adminId);
        app.setReviewedAt(LocalDateTime.now());
        if (request != null && request.getNotes() != null) {
            app.setAdminNotes(request.getNotes());
        }
        applicationRepository.save(app);

        // Upgrade user role to INSTRUCTOR
        User user = app.getUser();
        user.setRole(UserRole.INSTRUCTOR);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Instructor approved", "userId", user.getId()));
    }

    @PostMapping("/{id}/reject")
    @Transactional
    public ResponseEntity<?> reject(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) ReviewRequest request) {
        InstructorApplication app = applicationRepository.findById(id).orElse(null);
        if (app == null) return ResponseEntity.notFound().build();

        UUID adminId = UUID.fromString(jwt.getSubject());
        app.setStatus(ApplicationStatus.REJECTED);
        app.setReviewedBy(adminId);
        app.setReviewedAt(LocalDateTime.now());
        if (request != null && request.getNotes() != null) {
            app.setAdminNotes(request.getNotes());
        }
        applicationRepository.save(app);

        return ResponseEntity.ok(Map.of("message", "Application rejected"));
    }

    private ApplicationSummary toSummary(InstructorApplication app) {
        ApplicationSummary s = new ApplicationSummary();
        s.setId(app.getId().toString());
        s.setDisplayName(app.getUser().getFullName());
        s.setEmail(app.getUser().getEmail());
        s.setAvatarUrl(app.getUser().getAvatarUrl());
        s.setHeadline(app.getHeadline());
        s.setExpertise(app.getExpertise());
        s.setYearsTeaching(app.getYearsTeaching());
        s.setStatus(app.getStatus().name());
        s.setCreatedAt(app.getCreatedAt() != null ? app.getCreatedAt().toString() : null);
        return s;
    }

    private ApplicationDetail toDetail(InstructorApplication app) {
        ApplicationDetail d = new ApplicationDetail();
        d.setId(app.getId().toString());
        d.setUserId(app.getUser().getId().toString());
        d.setDisplayName(app.getUser().getFullName());
        d.setEmail(app.getUser().getEmail());
        d.setAvatarUrl(app.getUser().getAvatarUrl());
        d.setBio(app.getUser().getBio());
        d.setHeadline(app.getHeadline());
        d.setCvUrl(app.getCvUrl());
        d.setLinkedinUrl(app.getLinkedinUrl());
        d.setGithubUrl(app.getGithubUrl());
        d.setWebsiteUrl(app.getWebsiteUrl());
        d.setYearsTeaching(app.getYearsTeaching());
        d.setCurrentInstitution(app.getCurrentInstitution());
        d.setTeachingDescription(app.getTeachingDescription());
        d.setYoutubeChannelUrl(app.getYoutubeChannelUrl());
        d.setYoutubeSubscribers(app.getYoutubeSubscribers());
        d.setOtherPlatforms(app.getOtherPlatforms());
        d.setExpertise(app.getExpertise());
        d.setWhyTeach(app.getWhyTeach());
        d.setStatus(app.getStatus().name());
        d.setAdminNotes(app.getAdminNotes());
        d.setCreatedAt(app.getCreatedAt() != null ? app.getCreatedAt().toString() : null);
        d.setReviewedAt(app.getReviewedAt() != null ? app.getReviewedAt().toString() : null);
        return d;
    }

    @Data
    public static class ReviewRequest {
        private String notes;
    }

    @Data
    public static class ApplicationSummary {
        private String id;
        private String displayName;
        private String email;
        private String avatarUrl;
        private String headline;
        private String expertise;
        private Integer yearsTeaching;
        private String status;
        private String createdAt;
    }

    @Data
    public static class ApplicationDetail {
        private String id;
        private String userId;
        private String displayName;
        private String email;
        private String avatarUrl;
        private String bio;
        private String headline;
        private String cvUrl;
        private String linkedinUrl;
        private String githubUrl;
        private String websiteUrl;
        private Integer yearsTeaching;
        private String currentInstitution;
        private String teachingDescription;
        private String youtubeChannelUrl;
        private Integer youtubeSubscribers;
        private String otherPlatforms;
        private String expertise;
        private String whyTeach;
        private String status;
        private String adminNotes;
        private String createdAt;
        private String reviewedAt;
    }
}
