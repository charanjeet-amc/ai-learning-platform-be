package com.ailearning.platform.controller;

import com.ailearning.platform.entity.User;
import com.ailearning.platform.repository.UserRepository;
import com.ailearning.platform.exception.ResourceNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        User user = getUser(jwt);
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateProfileRequest request) {
        User user = getUser(jwt);

        if (request.getDisplayName() != null) user.setFullName(request.getDisplayName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getBio() != null) user.setBio(request.getBio());

        userRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/password")
    @Transactional
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ChangePasswordRequest request) {
        User user = getUser(jwt);

        if (user.getPasswordHash() != null
                && !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 6 characters"));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<Map<String, String>> deleteAccount(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) DeleteAccountRequest request) {
        User user = getUser(jwt);

        if (user.getPasswordHash() != null) {
            if (request == null || request.getPassword() == null
                    || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is incorrect"));
            }
        }

        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }

    private User getUser(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private ProfileResponse toResponse(User user) {
        ProfileResponse r = new ProfileResponse();
        r.setId(user.getId().toString());
        r.setUsername(user.getUsername());
        r.setEmail(user.getEmail());
        r.setDisplayName(user.getFullName());
        r.setAvatarUrl(user.getAvatarUrl());
        r.setBio(user.getBio());
        r.setRole(user.getRole().name());
        r.setSubscriptionTier(user.getSubscriptionTier().name());
        r.setTotalXp(user.getTotalXp());
        r.setCurrentStreak(user.getCurrentStreak());
        r.setLongestStreak(user.getLongestStreak());
        r.setHasPassword(user.getPasswordHash() != null);
        r.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return r;
    }

    @Data
    public static class ProfileResponse {
        private String id;
        private String username;
        private String email;
        private String displayName;
        private String avatarUrl;
        private String bio;
        private String role;
        private String subscriptionTier;
        private boolean hasPassword;
        private Long totalXp;
        private Integer currentStreak;
        private Integer longestStreak;
        private String createdAt;
    }

    @Data
    public static class UpdateProfileRequest {
        private String displayName;
        private String avatarUrl;
        private String bio;
    }

    @Data
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }

    @Data
    public static class DeleteAccountRequest {
        private String password;
    }
}
