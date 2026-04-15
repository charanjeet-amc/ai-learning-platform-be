package com.ailearning.platform.controller;

import com.ailearning.platform.entity.InstructorApplication;
import com.ailearning.platform.entity.User;
import com.ailearning.platform.entity.enums.ApplicationStatus;
import com.ailearning.platform.entity.enums.UserRole;
import com.ailearning.platform.repository.InstructorApplicationRepository;
import com.ailearning.platform.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/instructor-application")
@RequiredArgsConstructor
public class InstructorApplicationController {

    private final InstructorApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getMyApplication(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return applicationRepository.findByUserId(userId)
                .map(app -> ResponseEntity.ok(toResponse(app)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> submitApplication(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ApplicationRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        if (user.getRole() != UserRole.PENDING_INSTRUCTOR) {
            return ResponseEntity.badRequest().body(Map.of("error",
                "Only users registered as pending instructors can submit applications"));
        }

        InstructorApplication app = applicationRepository.findByUserId(userId)
                .orElseGet(() -> InstructorApplication.builder().user(user).build());

        // Update fields
        if (request.getHeadline() != null) app.setHeadline(request.getHeadline());
        if (request.getCvUrl() != null) app.setCvUrl(request.getCvUrl());
        if (request.getLinkedinUrl() != null) app.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl() != null) app.setGithubUrl(request.getGithubUrl());
        if (request.getWebsiteUrl() != null) app.setWebsiteUrl(request.getWebsiteUrl());
        if (request.getYearsTeaching() != null) app.setYearsTeaching(request.getYearsTeaching());
        if (request.getCurrentInstitution() != null) app.setCurrentInstitution(request.getCurrentInstitution());
        if (request.getTeachingDescription() != null) app.setTeachingDescription(request.getTeachingDescription());
        if (request.getYoutubeChannelUrl() != null) app.setYoutubeChannelUrl(request.getYoutubeChannelUrl());
        if (request.getYoutubeSubscribers() != null) app.setYoutubeSubscribers(request.getYoutubeSubscribers());
        if (request.getOtherPlatforms() != null) app.setOtherPlatforms(request.getOtherPlatforms());
        if (request.getExpertise() != null) app.setExpertise(request.getExpertise());
        if (request.getWhyTeach() != null) app.setWhyTeach(request.getWhyTeach());

        // Update user profile fields if provided
        if (request.getPhotoUrl() != null) user.setAvatarUrl(request.getPhotoUrl());
        if (request.getBio() != null) user.setBio(request.getBio());
        userRepository.save(user);

        app.setStatus(ApplicationStatus.PENDING);
        applicationRepository.save(app);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(app));
    }

    private ApplicationResponse toResponse(InstructorApplication app) {
        ApplicationResponse r = new ApplicationResponse();
        r.setId(app.getId().toString());
        r.setUserId(app.getUser().getId().toString());
        r.setDisplayName(app.getUser().getFullName());
        r.setEmail(app.getUser().getEmail());
        r.setAvatarUrl(app.getUser().getAvatarUrl());
        r.setHeadline(app.getHeadline());
        r.setCvUrl(app.getCvUrl());
        r.setLinkedinUrl(app.getLinkedinUrl());
        r.setGithubUrl(app.getGithubUrl());
        r.setWebsiteUrl(app.getWebsiteUrl());
        r.setYearsTeaching(app.getYearsTeaching());
        r.setCurrentInstitution(app.getCurrentInstitution());
        r.setTeachingDescription(app.getTeachingDescription());
        r.setYoutubeChannelUrl(app.getYoutubeChannelUrl());
        r.setYoutubeSubscribers(app.getYoutubeSubscribers());
        r.setOtherPlatforms(app.getOtherPlatforms());
        r.setExpertise(app.getExpertise());
        r.setWhyTeach(app.getWhyTeach());
        r.setStatus(app.getStatus().name());
        r.setAdminNotes(app.getAdminNotes());
        r.setCreatedAt(app.getCreatedAt() != null ? app.getCreatedAt().toString() : null);
        r.setReviewedAt(app.getReviewedAt() != null ? app.getReviewedAt().toString() : null);
        return r;
    }

    @Data
    public static class ApplicationRequest {
        private String headline;
        private String photoUrl;
        private String bio;
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
    }

    @Data
    public static class ApplicationResponse {
        private String id;
        private String userId;
        private String displayName;
        private String email;
        private String avatarUrl;
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
