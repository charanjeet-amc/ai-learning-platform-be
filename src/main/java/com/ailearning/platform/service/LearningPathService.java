package com.ailearning.platform.service;

import com.ailearning.platform.dto.response.LearningPathResponse;

import java.util.UUID;

public interface LearningPathService {
    LearningPathResponse getPersonalizedPath(UUID courseId, UUID userId);
    UUID getNextConcept(UUID courseId, UUID userId);
}
