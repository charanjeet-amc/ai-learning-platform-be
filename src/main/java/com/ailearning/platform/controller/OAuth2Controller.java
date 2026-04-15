package com.ailearning.platform.controller;

import com.ailearning.platform.config.JwtTokenProvider;
import com.ailearning.platform.dto.response.AuthResponse;
import com.ailearning.platform.entity.User;
import com.ailearning.platform.entity.enums.UserRole;
import com.ailearning.platform.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/auth/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // Google
    @Value("${oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${oauth2.google.client-secret:}")
    private String googleClientSecret;

    // GitHub
    @Value("${oauth2.github.client-id:}")
    private String githubClientId;

    @Value("${oauth2.github.client-secret:}")
    private String githubClientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    // ── Google OAuth2 ──────────────────────────────────────────────

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleCallback(@RequestBody OAuth2CodeRequest request) {
        try {
            // 1. Exchange code for access token
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", request.getCode());
            params.add("client_id", googleClientId);
            params.add("client_secret", googleClientSecret);
            params.add("redirect_uri", request.getRedirectUri());
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = restTemplate.postForObject(
                    "https://oauth2.googleapis.com/token",
                    new HttpEntity<>(params, headers),
                    Map.class);

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String accessToken = (String) tokenResponse.get("access_token");

            // 2. Fetch user info
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);

            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v2/userinfo",
                    HttpMethod.GET,
                    new HttpEntity<>(userHeaders),
                    Map.class).getBody();

            if (userInfo == null || userInfo.get("email") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = (String) userInfo.get("email");
            String name = (String) userInfo.getOrDefault("name", email.split("@")[0]);
            String picture = (String) userInfo.get("picture");
            String googleId = String.valueOf(userInfo.get("id"));

            // 3. Find or create user
            User user = findOrCreateOAuthUser(email, name, picture, "google-" + googleId);

            return ResponseEntity.ok(buildAuthResponse(user));

        } catch (Exception e) {
            log.error("Google OAuth2 error", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // ── GitHub OAuth2 ──────────────────────────────────────────────

    @PostMapping("/github")
    public ResponseEntity<AuthResponse> githubCallback(@RequestBody OAuth2CodeRequest request) {
        try {
            // 1. Exchange code for access token
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", request.getCode());
            params.add("client_id", githubClientId);
            params.add("client_secret", githubClientSecret);
            params.add("redirect_uri", request.getRedirectUri());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = restTemplate.postForObject(
                    "https://github.com/login/oauth/access_token",
                    new HttpEntity<>(params, headers),
                    Map.class);

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String accessToken = (String) tokenResponse.get("access_token");

            // 2. Fetch user info
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);

            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    new HttpEntity<>(userHeaders),
                    Map.class).getBody();

            if (userInfo == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = (String) userInfo.get("email");
            String login = (String) userInfo.get("login");
            String name = (String) userInfo.getOrDefault("name", login);
            String picture = (String) userInfo.get("avatar_url");
            String githubId = String.valueOf(userInfo.get("id"));

            // GitHub email may be private — fetch from emails endpoint
            if (email == null) {
                email = fetchGitHubEmail(accessToken);
            }
            if (email == null) {
                email = login + "@github.oauth";
            }

            // 3. Find or create user
            User user = findOrCreateOAuthUser(email, name, picture, "github-" + githubId);

            return ResponseEntity.ok(buildAuthResponse(user));

        } catch (Exception e) {
            log.error("GitHub OAuth2 error", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // ── Shared Helpers ─────────────────────────────────────────────

    private User findOrCreateOAuthUser(String email, String name, String avatarUrl, String oauthId) {
        // Try to find by email first (link existing account)
        return userRepository.findByEmail(email)
                .map(existing -> {
                    // Update avatar if not set
                    if (existing.getAvatarUrl() == null && avatarUrl != null) {
                        existing.setAvatarUrl(avatarUrl);
                        return userRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    // Create new user
                    String username = generateUniqueUsername(email, name);
                    User user = User.builder()
                            .keycloakId(oauthId)
                            .email(email)
                            .username(username)
                            .fullName(name)
                            .avatarUrl(avatarUrl)
                            .role(UserRole.STUDENT)
                            .build();
                    return userRepository.save(user);
                });
    }

    private String generateUniqueUsername(String email, String name) {
        // Try name-based username first
        String base = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.length() < 3) {
            base = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        }
        if (base.length() < 3) {
            base = "user";
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private AuthResponse buildAuthResponse(User user) {
        List<String> roles = List.of(user.getRole().name());
        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), roles);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .roles(roles)
                .build();
    }

    @SuppressWarnings("unchecked")
    private String fetchGitHubEmail(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            List<Map<String, Object>> emails = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    List.class).getBody();

            if (emails != null) {
                for (Map<String, Object> e : emails) {
                    if (Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified"))) {
                        return (String) e.get("email");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch GitHub emails", e);
        }
        return null;
    }

    @Data
    public static class OAuth2CodeRequest {
        private String code;
        private String redirectUri;
    }
}
