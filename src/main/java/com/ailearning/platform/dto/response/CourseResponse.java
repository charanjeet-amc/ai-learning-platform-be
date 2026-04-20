package com.ailearning.platform.dto.response;

import com.ailearning.platform.entity.enums.DifficultyLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CourseResponse {
    private UUID id;
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
    private Boolean published;
    private String status;
    private String adminFeedback;
    private String instructorNotes;
    private Double rating;
    private Long enrollmentCount;
    private Double price;
    private String createdByName;
    private List<ModuleResponse> modules;
    private LocalDateTime createdAt;
}
