package com.ailearning.platform.service;

import com.ailearning.platform.dto.response.EnrolledCourseResponse;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {
    void enroll(UUID userId, UUID courseId);
    void unenroll(UUID userId, UUID courseId);
    List<EnrolledCourseResponse> getUserEnrollments(UUID userId);
    boolean isEnrolled(UUID userId, UUID courseId);
    void updateProgress(UUID userId, UUID courseId);
}
