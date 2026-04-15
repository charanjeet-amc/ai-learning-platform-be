package com.ailearning.platform.service;

import com.ailearning.platform.dto.request.CreateCourseRequest;
import com.ailearning.platform.dto.response.CourseProgressResponse;
import com.ailearning.platform.dto.response.CourseResponse;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    CourseResponse createCourse(CreateCourseRequest request, UUID creatorId);
    CourseResponse getCourse(UUID courseId);
    CourseResponse getCourseWithTree(UUID courseId);
    Page<CourseResponse> listCourses(Pageable pageable);
    Page<CourseResponse> searchCourses(String query, Pageable pageable);
    Page<CourseResponse> filterCourses(String category, DifficultyLevel difficulty,
                                       Integer minDuration, Integer maxDuration,
                                       String q, Pageable pageable);
    List<String> getCategories();
    CourseResponse updateCourse(UUID courseId, CreateCourseRequest request, UUID userId);
    void publishCourse(UUID courseId, UUID userId);
    void deleteCourse(UUID courseId, UUID userId);
    CourseProgressResponse getCourseProgress(UUID courseId, UUID userId);
}
