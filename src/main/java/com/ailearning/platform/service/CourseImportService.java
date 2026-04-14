package com.ailearning.platform.service;

import com.ailearning.platform.entity.*;
import com.ailearning.platform.entity.Module;
import com.ailearning.platform.entity.enums.ContentType;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import com.ailearning.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Imports a PDF/DOCX document and creates a full course hierarchy:
 * Course → Modules → Topics → Concepts → LearningUnits
 *
 * Structure mapping:
 *   H1 / Level 1 → Module
 *   H2 / Level 2 → Topic
 *   H3 / Level 3 → Concept  (content under it → LearningUnit)
 *
 * If document has only one level of headings, each heading becomes a Topic
 * under a single Module, with auto-generated Concepts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseImportService {

    private final DocumentParserService documentParserService;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final TopicRepository topicRepository;
    private final ConceptRepository conceptRepository;
    private final LearningUnitRepository learningUnitRepository;

    @Transactional
    public Course importFromDocument(MultipartFile file, Course course) throws IOException {
        String slug = course.getSlug() != null ? course.getSlug() :
                course.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-");

        DocumentParserService.ParsedDocument parsed = documentParserService.parse(file, slug);
        List<DocumentParserService.Section> sections = parsed.sections();

        if (sections.isEmpty()) {
            throw new IllegalArgumentException("Could not extract any content from the document.");
        }

        // Determine the levels present
        Set<Integer> levels = new HashSet<>();
        for (var s : sections) levels.add(s.level());

        if (levels.contains(1) && levels.contains(2)) {
            // Full hierarchy: H1→Module, H2→Topic, content→Concept+LearningUnit
            buildFullHierarchy(course, sections);
        } else if (levels.contains(1)) {
            // Only H1: each H1→Topic under single Module, content→Concept
            buildFlatHierarchy(course, sections);
        } else {
            // Only H2/H3 or single section: each section→Topic
            buildFlatHierarchy(course, sections);
        }

        return courseRepository.save(course);
    }

    private void buildFullHierarchy(Course course, List<DocumentParserService.Section> sections) {
        Module currentModule = null;
        Topic currentTopic = null;
        int moduleIdx = 0, topicIdx = 0, conceptIdx = 0;

        for (var section : sections) {
            switch (section.level()) {
                case 1 -> {
                    currentModule = Module.builder()
                            .course(course)
                            .title(section.title())
                            .description(truncate(section.content(), 500))
                            .orderIndex(moduleIdx++)
                            .build();
                    currentModule = moduleRepository.save(currentModule);
                    course.getModules().add(currentModule);
                    topicIdx = 0;
                    conceptIdx = 0;
                    currentTopic = null;
                }
                case 2 -> {
                    if (currentModule == null) {
                        currentModule = Module.builder()
                                .course(course).title("Introduction")
                                .orderIndex(moduleIdx++).build();
                        currentModule = moduleRepository.save(currentModule);
                        course.getModules().add(currentModule);
                    }
                    currentTopic = Topic.builder()
                            .module(currentModule)
                            .title(section.title())
                            .orderIndex(topicIdx++)
                            .build();
                    currentTopic = topicRepository.save(currentTopic);
                    currentModule.getTopics().add(currentTopic);
                    conceptIdx = 0;

                    // If this topic section has content directly, create a concept for it
                    if (!section.content().isBlank()) {
                        createConceptWithContent(currentTopic, section.title(), section.content(), conceptIdx++);
                    }
                }
                case 3 -> {
                    if (currentTopic == null) {
                        if (currentModule == null) {
                            currentModule = Module.builder()
                                    .course(course).title("Introduction")
                                    .orderIndex(moduleIdx++).build();
                            currentModule = moduleRepository.save(currentModule);
                            course.getModules().add(currentModule);
                        }
                        currentTopic = Topic.builder()
                                .module(currentModule).title("General")
                                .orderIndex(topicIdx++).build();
                        currentTopic = topicRepository.save(currentTopic);
                        currentModule.getTopics().add(currentTopic);
                    }
                    createConceptWithContent(currentTopic, section.title(), section.content(), conceptIdx++);
                }
            }
        }
    }

    private void buildFlatHierarchy(Course course, List<DocumentParserService.Section> sections) {
        Module module = Module.builder()
                .course(course)
                .title(course.getTitle())
                .description(course.getDescription())
                .orderIndex(0)
                .build();
        module = moduleRepository.save(module);
        course.getModules().add(module);

        int topicIdx = 0;
        for (var section : sections) {
            Topic topic = Topic.builder()
                    .module(module)
                    .title(section.title())
                    .orderIndex(topicIdx++)
                    .build();
            topic = topicRepository.save(topic);
            module.getTopics().add(topic);

            createConceptWithContent(topic, section.title(), section.content(), 0);
        }
    }

    private void createConceptWithContent(Topic topic, String title, String content, int orderIndex) {
        Concept concept = Concept.builder()
                .topic(topic)
                .title(title)
                .definition(truncate(content, 500))
                .orderIndex(orderIndex)
                .difficultyLevel(DifficultyLevel.BEGINNER)
                .build();
        concept = conceptRepository.save(concept);
        topic.getConcepts().add(concept);

        // Create a TEXT learning unit with the full markdown content
        Map<String, Object> unitContent = new HashMap<>();
        unitContent.put("body", content);

        LearningUnit unit = LearningUnit.builder()
                .concept(concept)
                .title(title)
                .type(ContentType.TEXT)
                .content(unitContent)
                .orderIndex(0)
                .build();
        learningUnitRepository.save(unit);
        concept.getLearningUnits().add(unit);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}
