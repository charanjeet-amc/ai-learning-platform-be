package com.ailearning.platform.service.impl;

import com.ailearning.platform.dto.request.CreateCourseRequest;
import com.ailearning.platform.dto.response.*;
import com.ailearning.platform.entity.*;
import com.ailearning.platform.entity.Module;
import com.ailearning.platform.entity.enums.ConceptStatus;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.repository.*;
import com.ailearning.platform.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final UserConceptProgressRepository progressRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorId));

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .difficulty(request.getDifficulty())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .industryVertical(request.getIndustryVertical())
                .skillsOutcome(request.getSkillsOutcome())
                .prerequisites(request.getPrerequisites())
                .tags(request.getTags())
                .price(request.getPrice())
                .createdBy(creator)
                .build();

        course = courseRepository.save(course);
        return mapToResponse(course);
    }

    @Override
    @Cacheable(value = "courses", key = "#courseId")
    public CourseResponse getCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        return mapToResponse(course);
    }

    @Override
    public CourseResponse getCourseWithTree(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        return mapToResponseWithTree(course);
    }

    @Override
    public Page<CourseResponse> listCourses(Pageable pageable) {
        return courseRepository.findByPublishedTrue(pageable).map(this::mapToResponse);
    }

    @Override
    public Page<CourseResponse> searchCourses(String query, Pageable pageable) {
        return courseRepository.searchCourses(query, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public CourseResponse updateCourse(UUID courseId, CreateCourseRequest request, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setShortDescription(request.getShortDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setDifficulty(request.getDifficulty());
        course.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        course.setIndustryVertical(request.getIndustryVertical());
        course.setSkillsOutcome(request.getSkillsOutcome());
        course.setPrerequisites(request.getPrerequisites());
        course.setTags(request.getTags());
        course.setPrice(request.getPrice());

        course = courseRepository.save(course);
        return mapToResponse(course);
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public void publishCourse(UUID courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        course.setPublished(true);
        courseRepository.save(course);
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", key = "#courseId")
    public void deleteCourse(UUID courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        courseRepository.delete(course);
    }

    @Override
    public CourseProgressResponse getCourseProgress(UUID courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        List<UserConceptProgress> progressList =
                progressRepository.findByUserIdAndCourseId(userId, courseId);

        long totalConcepts = course.getModules().stream()
                .flatMap(m -> m.getTopics().stream())
                .flatMap(t -> t.getConcepts().stream())
                .count();

        long mastered = progressList.stream()
                .filter(p -> p.getStatus() == ConceptStatus.MASTERED)
                .count();

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElse(null);

        return CourseProgressResponse.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .overallProgress(totalConcepts > 0 ? (double) mastered / totalConcepts * 100 : 0)
                .totalConcepts(totalConcepts)
                .masteredConcepts(mastered)
                .currentConceptId(enrollment != null ? enrollment.getCurrentConceptId() : null)
                .build();
    }

    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .shortDescription(course.getShortDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .difficulty(course.getDifficulty())
                .estimatedDurationMinutes(course.getEstimatedDurationMinutes())
                .industryVertical(course.getIndustryVertical())
                .skillsOutcome(course.getSkillsOutcome())
                .prerequisites(course.getPrerequisites())
                .tags(course.getTags())
                .published(course.getPublished())
                .rating(course.getRating())
                .enrollmentCount(course.getEnrollmentCount())
                .price(course.getPrice())
                .createdByName(course.getCreatedBy() != null ? course.getCreatedBy().getFullName() : null)
                .createdAt(course.getCreatedAt())
                .build();
    }

    private CourseResponse mapToResponseWithTree(Course course) {
        CourseResponse response = mapToResponse(course);
        response.setModules(course.getModules().stream()
                .map(this::mapModuleResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private ModuleResponse mapModuleResponse(Module module) {
        return ModuleResponse.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(module.getDescription())
                .orderIndex(module.getOrderIndex())
                .learningObjectives(module.getLearningObjectives())
                .topics(module.getTopics().stream()
                        .map(this::mapTopicResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private TopicResponse mapTopicResponse(Topic topic) {
        return TopicResponse.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .orderIndex(topic.getOrderIndex())
                .estimatedTimeMinutes(topic.getEstimatedTimeMinutes())
                .tags(topic.getTags())
                .concepts(topic.getConcepts().stream()
                        .map(this::mapConceptResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private ConceptResponse mapConceptResponse(Concept concept) {
        return ConceptResponse.builder()
                .id(concept.getId())
                .title(concept.getTitle())
                .definition(concept.getDefinition())
                .difficultyLevel(concept.getDifficultyLevel())
                .orderIndex(concept.getOrderIndex())
                .tags(concept.getTags())
                .misconceptions(concept.getMisconceptions().stream()
                        .map(ConceptMisconception::getMisconception)
                        .collect(Collectors.toList()))
                .socraticQuestions(concept.getSocraticQuestions().stream()
                        .map(ConceptSocratic::getQuestion)
                        .collect(Collectors.toList()))
                .outcomes(concept.getOutcomes().stream()
                        .map(ConceptOutcome::getOutcome)
                        .collect(Collectors.toList()))
                .dependencyIds(concept.getDependencies().stream()
                        .map(Concept::getId)
                        .collect(Collectors.toList()))
                .build();
    }
}
