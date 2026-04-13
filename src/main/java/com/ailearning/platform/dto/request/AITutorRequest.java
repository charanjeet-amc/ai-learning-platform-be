package com.ailearning.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AITutorRequest {
    private UUID courseId;
    private UUID moduleId;
    private UUID topicId;
    @NotNull
    private UUID conceptId;
    @NotBlank
    private String query;
    private String sessionId;
}
