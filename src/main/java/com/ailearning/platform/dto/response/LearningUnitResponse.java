package com.ailearning.platform.dto.response;

import com.ailearning.platform.entity.enums.ContentType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class LearningUnitResponse {
    private UUID id;
    private ContentType contentType;
    private String title;
    private Map<String, Object> content;
    private Integer orderIndex;
    private Integer estimatedTimeMinutes;
}
