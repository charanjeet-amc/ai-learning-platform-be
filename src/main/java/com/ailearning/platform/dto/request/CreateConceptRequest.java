package com.ailearning.platform.dto.request;

import com.ailearning.platform.entity.enums.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateConceptRequest {
    @NotNull
    private UUID topicId;
    @NotBlank
    private String title;
    private String definition;
    private DifficultyLevel difficultyLevel;
    private Integer orderIndex;
    private String[] tags;
    private List<UUID> dependencyIds;
    private List<String> misconceptions;
    private List<String> socraticQuestions;
    private List<String> outcomes;
}
