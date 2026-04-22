package com.ailearning.platform.controller;

import com.ailearning.platform.dto.response.CertificateResponse;
import com.ailearning.platform.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping("/api/certificates/courses/{courseId}")
    public ResponseEntity<CertificateResponse> generate(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(certificateService.getOrGenerate(userId, courseId));
    }

    @GetMapping("/api/certificates/my")
    public ResponseEntity<List<CertificateResponse>> getMyCertificates(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(certificateService.getUserCertificates(userId));
    }

    @GetMapping("/api/public/certificates/{verificationCode}")
    public ResponseEntity<CertificateResponse> verify(@PathVariable String verificationCode) {
        return ResponseEntity.ok(certificateService.verify(verificationCode));
    }

    @GetMapping("/api/public/certificates/{verificationCode}/download")
    public ResponseEntity<byte[]> download(@PathVariable String verificationCode) {
        byte[] pdf = certificateService.generatePdf(verificationCode);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"certificate-" + verificationCode + ".pdf\"")
                .body(pdf);
    }
}
