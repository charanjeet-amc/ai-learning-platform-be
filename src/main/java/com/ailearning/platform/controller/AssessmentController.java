package com.ailearning.platform.controller;

import com.ailearning.platform.dto.request.SubmitAnswerRequest;
import com.ailearning.platform.dto.response.AnswerResultResponse;
import com.ailearning.platform.dto.response.QuestionResponse;
import com.ailearning.platform.service.AssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping("/concepts/{conceptId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            @PathVariable UUID conceptId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(assessmentService.getQuestionsForConcept(conceptId, userId));
    }

    @PostMapping("/submit")
    public ResponseEntity<AnswerResultResponse> submitAnswer(
            @Valid @RequestBody SubmitAnswerRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(assessmentService.submitAnswer(request, userId));
    }

    @GetMapping("/modules/{moduleId}/diagnostic")
    public ResponseEntity<List<QuestionResponse>> getDiagnosticTest(
            @PathVariable UUID moduleId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(assessmentService.generateDiagnosticTest(moduleId, userId));
    }
}
