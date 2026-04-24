package com.ailearning.platform.dto.response;

import com.ailearning.platform.entity.enums.ConceptStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProgressResponse {
    private UUID userId;
    private UUID courseId;
    private UUID conceptId;
    private String conceptTitle;
    private Double masteryLevel;
    private Double confidenceScore;
    private Integer attempts;
    private ConceptStatus status;
    private Boolean fastTracked;
    private LocalDateTime nextReviewAt;
}
