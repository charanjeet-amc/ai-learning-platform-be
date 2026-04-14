package com.ailearning.platform.dto.request;

import com.ailearning.platform.entity.enums.DifficultyLevel;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateConceptRequest {
    private UUID topicId;
    private String title;
    private String definition;
    private String content;  // Markdown body for the learning unit
    private DifficultyLevel difficultyLevel;
    private Integer orderIndex;
    private String[] tags;
    private List<UUID> dependencyIds;
    private List<String> misconceptions;
    private List<String> socraticQuestions;
    private List<String> outcomes;
}
