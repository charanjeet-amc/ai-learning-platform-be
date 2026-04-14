package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LearningPathResponse {
    private UUID courseId;
    private String courseTitle;
    private List<LearningPathStepResponse> steps;
    private Integer totalSteps;
    private Integer completedSteps;
    private UUID nextConceptId;
    private String nextConceptTitle;
}
