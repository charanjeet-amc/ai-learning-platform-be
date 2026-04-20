package com.ailearning.platform.service.impl;

import com.ailearning.platform.ai.LearningPathEngine;
import com.ailearning.platform.dto.response.LearningPathResponse;
import com.ailearning.platform.dto.response.LearningPathStepResponse;
import com.ailearning.platform.entity.Concept;
import com.ailearning.platform.entity.Course;
import com.ailearning.platform.entity.UserConceptProgress;
import com.ailearning.platform.entity.enums.ConceptStatus;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.repository.ConceptRepository;
import com.ailearning.platform.repository.CourseRepository;
import com.ailearning.platform.repository.UserConceptProgressRepository;
import com.ailearning.platform.service.LearningPathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LearningPathServiceImpl implements LearningPathService {

    private final CourseRepository courseRepository;
    private final ConceptRepository conceptRepository;
    private final UserConceptProgressRepository progressRepository;
    private final LearningPathEngine learningPathEngine;

    @Override
    public LearningPathResponse getPersonalizedPath(UUID courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        // Fetch all concepts directly to avoid lazy loading issues
        List<Concept> allConcepts = conceptRepository.findAllByCourseId(courseId);
        log.info("Learning path: found {} concepts for course {}", allConcepts.size(), courseId);
        allConcepts.forEach(c -> log.info("  concept: {} ({})", c.getTitle(), c.getId()));

        // Get user progress
        Map<UUID, UserConceptProgress> progressMap = new HashMap<>();
        for (Concept c : allConcepts) {
            progressRepository.findByUserIdAndConceptId(userId, c.getId())
                    .ifPresent(p -> progressMap.put(c.getId(), p));
        }

        // Generate personalized order
        List<UUID> orderedIds = learningPathEngine.generatePath(userId, allConcepts);
        log.info("Learning path order for user {}: {}", userId,
                orderedIds.stream().map(id -> {
                    Concept c = allConcepts.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
                    UserConceptProgress p = progressMap.get(id);
                    return String.format("%s (mastery=%.2f, status=%s)",
                            c != null ? c.getTitle() : id,
                            p != null && p.getMasteryLevel() != null ? p.getMasteryLevel() : 0.0,
                            p != null ? p.getStatus() : "NO_PROGRESS");
                }).collect(Collectors.joining(" -> ")));

        // Build concept lookup
        Map<UUID, Concept> conceptMap = allConcepts.stream()
                .collect(Collectors.toMap(Concept::getId, c -> c));

        // Build steps
        List<LearningPathStepResponse> steps = new ArrayList<>();
        int completedCount = 0;
        UUID nextConceptId = null;
        String nextConceptTitle = null;

        for (int i = 0; i < orderedIds.size(); i++) {
            UUID cid = orderedIds.get(i);
            Concept concept = conceptMap.get(cid);
            if (concept == null) continue;

            UserConceptProgress progress = progressMap.get(cid);
            ConceptStatus status = progress != null ? progress.getStatus() : ConceptStatus.LOCKED;
            double mastery = progress != null && progress.getMasteryLevel() != null
                    ? progress.getMasteryLevel() : 0.0;

            String reason = determineReason(progress, mastery);

            if (status == ConceptStatus.MASTERED) {
                completedCount++;
            } else if (nextConceptId == null) {
                nextConceptId = cid;
                nextConceptTitle = concept.getTitle();
            }

            steps.add(LearningPathStepResponse.builder()
                    .stepIndex(i + 1)
                    .conceptId(cid)
                    .conceptTitle(concept.getTitle())
                    .difficulty(concept.getDifficultyLevel())
                    .status(status)
                    .masteryLevel(mastery)
                    .reason(reason)
                    .build());
        }

        return LearningPathResponse.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .steps(steps)
                .totalSteps(steps.size())
                .completedSteps(completedCount)
                .nextConceptId(nextConceptId)
                .nextConceptTitle(nextConceptTitle)
                .build();
    }

    @Override
    public UUID getNextConcept(UUID courseId, UUID userId) {
        List<Concept> allConcepts = conceptRepository.findAllByCourseId(courseId);
        return learningPathEngine.getNextConcept(userId, allConcepts);
    }

    private String determineReason(UserConceptProgress progress, double mastery) {
        if (progress == null) return "new";
        if (mastery < 0.4) return "remediation";
        if (mastery < 0.6) return "in_progress";
        if (mastery < 0.85) return "reinforcement";
        return "mastered";
    }
}
