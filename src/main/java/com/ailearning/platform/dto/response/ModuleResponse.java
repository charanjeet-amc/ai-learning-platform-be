package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ModuleResponse {
    private UUID id;
    private String title;
    private String description;
    private Integer orderIndex;
    private String[] learningObjectives;
    private List<TopicResponse> topics;
}
