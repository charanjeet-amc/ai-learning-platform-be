package com.ailearning.platform.controller;

import com.ailearning.platform.dto.response.LearningPathResponse;
import com.ailearning.platform.service.LearningPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/learning-path")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService learningPathService;

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<LearningPathResponse> getPersonalizedPath(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(learningPathService.getPersonalizedPath(courseId, userId));
    }

    @GetMapping("/courses/{courseId}/next")
    public ResponseEntity<Map<String, Object>> getNextConcept(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UUID nextConceptId = learningPathService.getNextConcept(courseId, userId);
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "nextConceptId", nextConceptId != null ? nextConceptId : ""
        ));
    }
}
