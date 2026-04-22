package com.ailearning.platform.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CertificateResponse(
        UUID id,
        String courseId,
        String courseTitle,
        String courseThumbnailUrl,
        String userName,
        String verificationCode,
        String verificationUrl,
        LocalDateTime issuedAt,
        LocalDateTime completedAt,
        Double progressPercent
) {}
