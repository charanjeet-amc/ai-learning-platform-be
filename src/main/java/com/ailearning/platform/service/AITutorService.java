package com.ailearning.platform.service;

import com.ailearning.platform.dto.request.AITutorRequest;
import com.ailearning.platform.dto.response.AITutorResponse;

import java.util.UUID;

public interface AITutorService {
    AITutorResponse chat(AITutorRequest request, UUID userId);
}
