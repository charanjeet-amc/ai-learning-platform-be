package com.ailearning.platform.dto.response;

import com.ailearning.platform.entity.enums.ConceptStatus;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LearningPathStepResponse {
    private Integer stepIndex;
    private UUID conceptId;
    private String conceptTitle;
    private DifficultyLevel difficulty;
    private ConceptStatus status;
    private Double masteryLevel;
    private String reason; // "remediation", "in_progress", "new", "reinforcement"
    private Boolean fastTracked;
}
