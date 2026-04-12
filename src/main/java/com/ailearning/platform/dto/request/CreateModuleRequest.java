package com.ailearning.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateModuleRequest {
    @NotNull
    private UUID courseId;
    @NotBlank
    private String title;
    private String description;
    private Integer orderIndex;
    private String[] learningObjectives;
}
