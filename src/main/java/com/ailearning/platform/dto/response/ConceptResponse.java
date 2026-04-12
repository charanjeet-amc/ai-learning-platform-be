package com.ailearning.platform.dto.response;

import com.ailearning.platform.entity.enums.DifficultyLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ConceptResponse {
    private UUID id;
    private String title;
    private String definition;
    private DifficultyLevel difficultyLevel;
    private Integer orderIndex;
    private String[] tags;
    private List<LearningUnitResponse> learningUnits;
    private List<String> misconceptions;
    private List<String> socraticQuestions;
    private List<String> outcomes;
    private List<UUID> dependencyIds;
}
