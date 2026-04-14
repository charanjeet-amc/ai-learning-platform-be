package com.ailearning.platform.service;

import com.ailearning.platform.dto.request.SubmitAnswerRequest;
import com.ailearning.platform.dto.response.AnswerResultResponse;
import com.ailearning.platform.dto.response.QuestionResponse;
import com.ailearning.platform.dto.response.UserProgressResponse;

import java.util.List;
import java.util.UUID;

public interface AssessmentService {
    List<QuestionResponse> getQuestionsForConcept(UUID conceptId, UUID userId);
    AnswerResultResponse submitAnswer(SubmitAnswerRequest request, UUID userId);
    List<QuestionResponse> generateDiagnosticTest(UUID moduleId, UUID userId);
    List<UserProgressResponse> getReviewQueue(UUID userId);
    List<QuestionResponse> generateAIQuestions(UUID conceptId, UUID userId);
}
