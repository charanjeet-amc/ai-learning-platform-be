package com.ailearning.platform.dto.request;

import com.ailearning.platform.entity.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class CreateLearningUnitRequest {
    @NotBlank
    private String title;
    private ContentType contentType;
    private Map<String, Object> content;
    private Integer orderIndex;
    private Integer estimatedTimeMinutes;
}
