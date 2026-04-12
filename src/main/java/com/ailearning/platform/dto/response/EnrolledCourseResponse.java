package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EnrolledCourseResponse {
    private UUID courseId;
    private String courseTitle;
    private String thumbnailUrl;
    private Double progressPercent;
    private Boolean completed;
    private LocalDateTime enrolledAt;
}
