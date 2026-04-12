package com.ailearning.platform.controller;

import com.ailearning.platform.dto.request.AITutorRequest;
import com.ailearning.platform.dto.response.AITutorResponse;
import com.ailearning.platform.service.AITutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tutor")
@RequiredArgsConstructor
public class AITutorController {

    private final AITutorService aiTutorService;

    @PostMapping("/chat")
    public ResponseEntity<AITutorResponse> chat(
            @Valid @RequestBody AITutorRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(aiTutorService.chat(request, userId));
    }
}
