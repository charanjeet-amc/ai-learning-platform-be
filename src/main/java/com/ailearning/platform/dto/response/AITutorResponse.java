package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AITutorResponse {
    private String message;
    private String responseType; // explanation, hint, question, step_by_step, answer
    private Integer hintLevel; // 1-4
    private String suggestedAction; // next_concept, reinforce, remediate
    private String conceptId;
}
