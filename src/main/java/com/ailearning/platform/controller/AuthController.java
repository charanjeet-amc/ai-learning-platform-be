package com.ailearning.platform.controller;

import com.ailearning.platform.config.JwtTokenProvider;
import com.ailearning.platform.dto.request.LoginRequest;
import com.ailearning.platform.dto.request.RegisterRequest;
import com.ailearning.platform.dto.response.AuthResponse;
import com.ailearning.platform.entity.User;
import com.ailearning.platform.repository.UserRepository;
import com.ailearning.platform.entity.enums.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/api/public/auth/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AuthResponse.builder().build());
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AuthResponse.builder().build());
        }

        User user = User.builder()
                .keycloakId("local-" + UUID.randomUUID())
                .email(request.getEmail())
                .username(request.getUsername())
                .fullName(request.getDisplayName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        List<String> roles = List.of(user.getRole().name());
        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), roles);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildAuthResponse(user, token, roles));
    }

    @PostMapping("/api/public/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElse(null);

        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> roles = List.of(user.getRole().name());
        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), roles);

        return ResponseEntity.ok(buildAuthResponse(user, token, roles));
    }

    @PostMapping("/api/public/auth/register-instructor")
    public ResponseEntity<AuthResponse> registerInstructor(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AuthResponse.builder().build());
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AuthResponse.builder().build());
        }

        User user = User.builder()
                .keycloakId("local-" + UUID.randomUUID())
                .email(request.getEmail())
                .username(request.getUsername())
                .fullName(request.getDisplayName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.PENDING_INSTRUCTOR)
                .build();

        user = userRepository.save(user);

        List<String> roles = List.of(user.getRole().name());
        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), roles);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildAuthResponse(user, token, roles));
    }

    private AuthResponse buildAuthResponse(User user, String token, List<String> roles) {
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
}
