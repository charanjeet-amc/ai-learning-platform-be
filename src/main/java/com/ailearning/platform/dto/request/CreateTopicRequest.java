package com.ailearning.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateTopicRequest {
    @NotNull
    private UUID moduleId;
    @NotBlank
    private String title;
    private Integer orderIndex;
    private Integer estimatedTimeMinutes;
    private String[] tags;
}
