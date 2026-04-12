package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CourseProgressResponse {
    private UUID courseId;
    private String courseTitle;
    private Double overallProgress;
    private Long totalConcepts;
    private Long masteredConcepts;
    private UUID currentConceptId;
    private List<ModuleProgressResponse> moduleProgress;
}

