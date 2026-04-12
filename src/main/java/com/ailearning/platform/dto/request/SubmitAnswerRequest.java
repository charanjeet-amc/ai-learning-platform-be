package com.ailearning.platform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class SubmitAnswerRequest {
    @NotNull
    private UUID questionId;
    @NotNull
    private Map<String, Object> answer;
    private Long timeTakenSeconds;
    private Integer hintsUsed;
}
