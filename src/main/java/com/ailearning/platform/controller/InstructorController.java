package com.ailearning.platform.controller;

import com.ailearning.platform.dto.request.CreateCourseRequest;
import com.ailearning.platform.dto.request.CreateModuleRequest;
import com.ailearning.platform.dto.request.CreateTopicRequest;
import com.ailearning.platform.dto.request.CreateConceptRequest;
import com.ailearning.platform.dto.response.CourseResponse;
import com.ailearning.platform.entity.*;
import com.ailearning.platform.entity.Module;
import com.ailearning.platform.entity.enums.ContentType;
import com.ailearning.platform.entity.enums.CourseStatus;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import com.ailearning.platform.entity.enums.UserRole;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.repository.*;
import com.ailearning.platform.service.S3StorageService;
import com.ailearning.platform.service.CourseImportService;
import com.ailearning.platform.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
@Slf4j
public class InstructorController {

    private final CourseService courseService;
    private final CourseImportService courseImportService;
    private final S3StorageService s3StorageService;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final TopicRepository topicRepository;
    private final ConceptRepository conceptRepository;
    private final LearningUnitRepository learningUnitRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    // ─── My Courses ────────────────────────────────────────────

    @GetMapping("/courses")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CourseResponse>> getMyCourses(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        ensureInstructor(userId);
        List<Course> courses = user.getRole() == UserRole.ADMIN
                ? courseRepository.findAll()
                : courseRepository.findByCreatedById(userId);
        return ResponseEntity.ok(courses.stream().map(this::mapCourseResponse).toList());
    }

    @GetMapping("/courses/{courseId}")
    @Transactional(readOnly = true)
    public ResponseEntity<CourseResponse> getCourseDetail(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Course course = getCourseOwnedBy(courseId, userId);
        return ResponseEntity.ok(mapCourseResponseWithTree(course));
    }

    // ─── Course CRUD ───────────────────────────────────────────

    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        CourseResponse response = courseService.createCourse(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/courses/{courseId}")
    @Transactional
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Course course = getCourseOwnedBy(courseId, userId);
        resetToDraftIfPublished(course);
        return ResponseEntity.ok(courseService.updateCourse(courseId, request, userId));
    }

    @PostMapping("/courses/{courseId}/submit-for-approval")
    @Transactional
    public ResponseEntity<Void> submitForApproval(
            @PathVariable UUID courseId,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Course course = getCourseOwnedBy(courseId, userId);
        CourseStatus status = course.getStatus() != null ? course.getStatus() : CourseStatus.DRAFT;
        if (status != CourseStatus.DRAFT && status != CourseStatus.CHANGES_REQUESTED) {
            return ResponseEntity.badRequest().build();
        }
        course.setStatus(CourseStatus.PENDING_APPROVAL);
        course.setAdminFeedback(null);
        if (body != null && body.containsKey("instructorNotes")) {
            course.setInstructorNotes(body.get("instructorNotes"));
        }
        courseRepository.save(course);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        ensureInstructor(userId);
        Course course = getCourseOwnedBy(courseId, userId);
        // Only admin can delete published/pending courses; instructors can only delete drafts
        if (user.getRole() != UserRole.ADMIN) {
            CourseStatus delStatus = course.getStatus() != null ? course.getStatus() : CourseStatus.DRAFT;
            if (delStatus != CourseStatus.DRAFT && delStatus != CourseStatus.CHANGES_REQUESTED) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Only admin can delete published or pending courses");
            }
        }
        courseService.deleteCourse(courseId, userId);
        return ResponseEntity.noContent().build();
    }

    // ─── Document Import ───────────────────────────────────────

    @PostMapping(value = "/courses/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<CourseResponse> importCourse(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "difficulty", required = false, defaultValue = "BEGINNER") String difficulty,
            @AuthenticationPrincipal Jwt jwt) throws IOException {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Course course = Course.builder()
                .title(title)
                .slug(generateUniqueSlug(title))
                .description(description)
                .difficulty(DifficultyLevel.valueOf(difficulty))
                .createdBy(creator)
                .build();
        course = courseRepository.save(course);

        course = courseImportService.importFromDocument(file, course);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapCourseResponseWithTree(course));
    }

    @PostMapping(value = "/courses/{courseId}/import-content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<CourseResponse> importContentToExistingCourse(
            @PathVariable UUID courseId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) throws IOException {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Course course = getCourseOwnedBy(courseId, userId);

        course = courseImportService.importFromDocument(file, course);
        return ResponseEntity.ok(mapCourseResponseWithTree(course));
    }

    // ─── Media Upload ──────────────────────────────────────────

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "general") String folder,
            @AuthenticationPrincipal Jwt jwt) throws IOException {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        String url = s3StorageService.uploadFile(file, folder);
        return ResponseEntity.ok(Map.of("url", url));
    }

    // ─── Module CRUD ───────────────────────────────────────────

    @PostMapping("/courses/{courseId}/modules")
    @Transactional
    public ResponseEntity<Map<String, Object>> addModule(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateModuleRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Course course = getCourseOwnedBy(courseId, userId);
        resetToDraftIfPublished(course);

        int nextOrder = course.getModules().size();
        Module module = Module.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : nextOrder)
                .build();
        module = moduleRepository.save(module);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", module.getId(),
                "title", module.getTitle(),
                "orderIndex", module.getOrderIndex()
        ));
    }

    @PutMapping("/modules/{moduleId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateModule(
            @PathVariable UUID moduleId,
            @Valid @RequestBody CreateModuleRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
        ensureOwnership(module.getCourse(), userId);
        resetToDraftIfPublished(module.getCourse());

        module.setTitle(request.getTitle());
        if (request.getDescription() != null) module.setDescription(request.getDescription());
        if (request.getOrderIndex() != null) module.setOrderIndex(request.getOrderIndex());
        if (request.getLearningObjectives() != null) module.setLearningObjectives(request.getLearningObjectives());
        moduleRepository.save(module);

        return ResponseEntity.ok(Map.of(
                "id", module.getId(),
                "title", module.getTitle(),
                "orderIndex", module.getOrderIndex()
        ));
    }

    @DeleteMapping("/modules/{moduleId}")
    @Transactional
    public ResponseEntity<Void> deleteModule(@PathVariable UUID moduleId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
        ensureOwnership(module.getCourse(), userId);
        resetToDraftIfPublished(module.getCourse());
        moduleRepository.delete(module);
        return ResponseEntity.noContent().build();
    }

    // ─── Topic CRUD ────────────────────────────────────────────

    @PostMapping("/modules/{moduleId}/topics")
    @Transactional
    public ResponseEntity<Map<String, Object>> addTopic(
            @PathVariable UUID moduleId,
            @Valid @RequestBody CreateTopicRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", "id", moduleId));
        ensureOwnership(module.getCourse(), userId);
        resetToDraftIfPublished(module.getCourse());

        int nextOrder = module.getTopics().size();
        Topic topic = Topic.builder()
                .module(module)
                .title(request.getTitle())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : nextOrder)
                .build();
        topic = topicRepository.save(topic);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", topic.getId(),
                "title", topic.getTitle(),
                "orderIndex", topic.getOrderIndex()
        ));
    }

    @PutMapping("/topics/{topicId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateTopic(
            @PathVariable UUID topicId,
            @Valid @RequestBody CreateTopicRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", topicId));
        ensureOwnership(topic.getModule().getCourse(), userId);
        resetToDraftIfPublished(topic.getModule().getCourse());

        topic.setTitle(request.getTitle());
        if (request.getOrderIndex() != null) topic.setOrderIndex(request.getOrderIndex());
        if (request.getEstimatedTimeMinutes() != null) topic.setEstimatedTimeMinutes(request.getEstimatedTimeMinutes());
        if (request.getTags() != null) topic.setTags(request.getTags());
        topicRepository.save(topic);

        return ResponseEntity.ok(Map.of(
                "id", topic.getId(),
                "title", topic.getTitle(),
                "orderIndex", topic.getOrderIndex()
        ));
    }

    @DeleteMapping("/topics/{topicId}")
    @Transactional
    public ResponseEntity<Void> deleteTopic(@PathVariable UUID topicId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", topicId));
        ensureOwnership(topic.getModule().getCourse(), userId);
        resetToDraftIfPublished(topic.getModule().getCourse());
        topicRepository.delete(topic);
        return ResponseEntity.noContent().build();
    }

    // ─── Concept CRUD ──────────────────────────────────────────

    @PostMapping("/topics/{topicId}/concepts")
    @Transactional
    public ResponseEntity<Map<String, Object>> addConcept(
            @PathVariable UUID topicId,
            @Valid @RequestBody CreateConceptRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", topicId));
        ensureOwnership(topic.getModule().getCourse(), userId);

        int nextOrder = topic.getConcepts().size();
        Concept concept = Concept.builder()
                .topic(topic)
                .title(request.getTitle())
                .definition(request.getDefinition())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : nextOrder)
                .difficultyLevel(request.getDifficultyLevel() != null ? request.getDifficultyLevel() : DifficultyLevel.BEGINNER)
                .build();
        concept = conceptRepository.save(concept);

        // If content is provided, create a learning unit
        if (request.getContent() != null && !request.getContent().isBlank()) {
            Map<String, Object> unitContent = new HashMap<>();
            unitContent.put("body", request.getContent());
            LearningUnit unit = LearningUnit.builder()
                    .concept(concept)
                    .title(request.getTitle())
                    .type(ContentType.TEXT)
                    .content(unitContent)
                    .orderIndex(0)
                    .build();
            learningUnitRepository.save(unit);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", concept.getId(),
                "title", concept.getTitle(),
                "orderIndex", concept.getOrderIndex()
        ));
    }

    @PutMapping("/concepts/{conceptId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateConcept(
            @PathVariable UUID conceptId,
            @Valid @RequestBody CreateConceptRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Concept concept = conceptRepository.findById(conceptId)
                .orElseThrow(() -> new ResourceNotFoundException("Concept", "id", conceptId));
        ensureOwnership(concept.getTopic().getModule().getCourse(), userId);
        resetToDraftIfPublished(concept.getTopic().getModule().getCourse());

        concept.setTitle(request.getTitle());
        if (request.getDefinition() != null) concept.setDefinition(request.getDefinition());
        if (request.getDifficultyLevel() != null) concept.setDifficultyLevel(request.getDifficultyLevel());
        if (request.getOrderIndex() != null) concept.setOrderIndex(request.getOrderIndex());
        conceptRepository.save(concept);

        // Update the first learning unit content if provided
        if (request.getContent() != null) {
            List<LearningUnit> units = learningUnitRepository.findByConceptIdOrderByOrderIndexAsc(conceptId);
            LearningUnit unit;
            if (!units.isEmpty()) {
                unit = units.get(0);
            } else {
                unit = LearningUnit.builder()
                        .concept(concept).title(concept.getTitle())
                        .type(ContentType.TEXT).orderIndex(0).build();
            }
            Map<String, Object> content = new HashMap<>();
            content.put("body", request.getContent());
            unit.setContent(content);
            learningUnitRepository.save(unit);
        }

        return ResponseEntity.ok(Map.of(
                "id", concept.getId(),
                "title", concept.getTitle(),
                "orderIndex", concept.getOrderIndex()
        ));
    }

    @DeleteMapping("/concepts/{conceptId}")
    @Transactional
    public ResponseEntity<Void> deleteConcept(@PathVariable UUID conceptId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        ensureInstructor(userId);
        Concept concept = conceptRepository.findById(conceptId)
                .orElseThrow(() -> new ResourceNotFoundException("Concept", "id", conceptId));
        ensureOwnership(concept.getTopic().getModule().getCourse(), userId);
        resetToDraftIfPublished(concept.getTopic().getModule().getCourse());
        conceptRepository.delete(concept);
        return ResponseEntity.noContent().build();
    }

    // ─── Helper Methods ────────────────────────────────────────

    private UUID extractUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private void ensureInstructor(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRole() != UserRole.INSTRUCTOR && user.getRole() != UserRole.ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only instructors can access this resource");
        }
    }

    private Course getCourseOwnedBy(UUID courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        ensureOwnership(course, userId);
        return course;
    }

    private void ensureOwnership(Course course, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRole() == UserRole.ADMIN) return; // Admins can manage any course
        if (course.getCreatedBy() == null || !course.getCreatedBy().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You don't have permission to modify this course");
        }
    }

    private void resetToDraftIfPublished(Course course) {
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            course.setStatus(CourseStatus.DRAFT);
            course.setPublished(false);
            course.setAdminFeedback(null);
            courseRepository.save(course);
        }
    }

    private CourseResponse mapCourseResponse(Course course) {
        String createdByName = null;
        try {
            if (course.getCreatedBy() != null) {
                createdByName = course.getCreatedBy().getFullName();
            }
        } catch (Exception e) {
            // Lazy loading may fail outside transaction
        }
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
                .status(course.getStatus() != null ? course.getStatus().name() : "DRAFT")
                .adminFeedback(course.getAdminFeedback())
                .instructorNotes(course.getInstructorNotes())
                .rating(course.getRating())
                .enrollmentCount(course.getEnrollmentCount())
                .price(course.getPrice())
                .createdByName(createdByName)
                .createdAt(course.getCreatedAt())
                .build();
    }

    private CourseResponse mapCourseResponseWithTree(Course course) {
        return courseService.getCourseWithTree(course.getId());
    }

    private String generateUniqueSlug(String title) {
        String base = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        String slug = base;
        int counter = 1;
        while (courseRepository.existsBySlug(slug)) {
            slug = base + "-" + counter;
            counter++;
        }
        return slug;
    }
}
