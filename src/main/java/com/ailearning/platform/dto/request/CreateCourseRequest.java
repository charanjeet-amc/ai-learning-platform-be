package com.ailearning.platform.dto.request;

import com.ailearning.platform.entity.enums.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCourseRequest {
    @NotBlank
    private String title;
    private String description;
    private String shortDescription;
    private String thumbnailUrl;
    private DifficultyLevel difficulty;
    private Integer estimatedDurationMinutes;
    private String industryVertical;
    private String[] skillsOutcome;
    private String prerequisites;
    private String[] tags;
    private String category;
    private Double price;
}
