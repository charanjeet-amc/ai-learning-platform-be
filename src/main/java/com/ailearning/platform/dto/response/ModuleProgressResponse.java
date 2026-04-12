package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ModuleProgressResponse {
    private UUID moduleId;
    private String moduleTitle;
    private Double progress;
    private Long totalConcepts;
    private Long masteredConcepts;
}
