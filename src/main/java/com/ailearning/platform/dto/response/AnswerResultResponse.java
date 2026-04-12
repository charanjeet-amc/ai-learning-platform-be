package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AnswerResultResponse {
    private UUID attemptId;
    private Boolean correct;
    private Double score;
    private String explanation;
    private String feedback;
    private Double updatedMastery;
    private String nextAction; // next_question, reinforce, remediate, advance
}
