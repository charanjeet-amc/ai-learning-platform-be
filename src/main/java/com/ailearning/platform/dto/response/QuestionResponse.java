package com.ailearning.platform.dto.response;

import com.ailearning.platform.entity.enums.QuestionType;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class QuestionResponse {
    private UUID id;
    private UUID conceptId;
    private QuestionType type;
    private String questionText;
    private Map<String, Object> metadata;
    private DifficultyLevel difficulty;
    private Boolean aiGenerated;
}
