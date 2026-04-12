package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TopicResponse {
    private UUID id;
    private String title;
    private Integer orderIndex;
    private Integer estimatedTimeMinutes;
    private String[] tags;
    private List<ConceptResponse> concepts;
}
