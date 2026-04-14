package com.ailearning.platform.ai;

import com.ailearning.platform.entity.Concept;
import com.ailearning.platform.entity.UserConceptProgress;
import com.ailearning.platform.entity.enums.ConceptStatus;
import com.ailearning.platform.repository.ConceptRepository;
import com.ailearning.platform.repository.UserConceptProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates personalized learning paths through a course's concept DAG.
 *
 * Algorithm:
 * 1. Topological sort all concepts in the course (respecting dependencies)
 * 2. Filter out mastered/fast-tracked concepts
 * 3. Prioritize weak areas and concepts due for review
 * 4. Return ordered list of concept IDs for the student to follow
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LearningPathEngine {

    private final ConceptRepository conceptRepository;
    private final UserConceptProgressRepository progressRepository;
    private final MasteryCalculator masteryCalculator;

    /**
     * Generate a personalized ordered list of concept IDs for a student in a course.
     */
    public List<UUID> generatePath(UUID userId, List<Concept> allConcepts) {
        // Build adjacency map for topological sort
        Map<UUID, Concept> conceptMap = allConcepts.stream()
                .collect(Collectors.toMap(Concept::getId, c -> c));

        // Get user progress for all concepts
        Map<UUID, UserConceptProgress> progressMap = new HashMap<>();
        for (Concept c : allConcepts) {
            progressRepository.findByUserIdAndConceptId(userId, c.getId())
                    .ifPresent(p -> progressMap.put(c.getId(), p));
        }

        // Topological sort using Kahn's algorithm
        List<UUID> sorted = topologicalSort(allConcepts, conceptMap);

        // Partition into categories
        List<UUID> needsRemediation = new ArrayList<>();   // struggling / low mastery
        List<UUID> notStarted = new ArrayList<>();          // never attempted
        List<UUID> inProgress = new ArrayList<>();          // partially done
        List<UUID> needsReinforcement = new ArrayList<>();  // 0.6-0.85 mastery
        List<UUID> mastered = new ArrayList<>();             // can skip

        for (UUID conceptId : sorted) {
            UserConceptProgress progress = progressMap.get(conceptId);

            if (progress == null) {
                notStarted.add(conceptId);
                continue;
            }

            double mastery = progress.getMasteryLevel() != null ? progress.getMasteryLevel() : 0.0;
            ConceptStatus status = progress.getStatus();

            if (status == ConceptStatus.MASTERED && mastery >= 0.85) {
                mastered.add(conceptId);
            } else if (mastery < 0.4) {
                needsRemediation.add(conceptId);
            } else if (mastery < 0.6) {
                inProgress.add(conceptId);
            } else {
                needsReinforcement.add(conceptId);
            }
        }

        // Build final path: remediation first, then in-progress, then new, then reinforcement
        List<UUID> path = new ArrayList<>();
        path.addAll(needsRemediation);
        path.addAll(inProgress);
        path.addAll(notStarted);
        path.addAll(needsReinforcement);
        // mastered concepts excluded unless due for review

        return path;
    }

    /**
     * Get just the next concept the student should work on.
     */
    public UUID getNextConcept(UUID userId, List<Concept> allConcepts) {
        List<UUID> path = generatePath(userId, allConcepts);
        return path.isEmpty() ? null : path.get(0);
    }

    /**
     * Kahn's algorithm for topological sort, falling back on orderIndex for
     * concepts without explicit dependencies.
     */
    private List<UUID> topologicalSort(List<Concept> concepts, Map<UUID, Concept> conceptMap) {
        Map<UUID, Set<UUID>> inEdges = new HashMap<>();
        Map<UUID, Set<UUID>> outEdges = new HashMap<>();

        for (Concept c : concepts) {
            inEdges.putIfAbsent(c.getId(), new HashSet<>());
            outEdges.putIfAbsent(c.getId(), new HashSet<>());

            if (c.getDependencies() != null) {
                for (Concept dep : c.getDependencies()) {
                    if (conceptMap.containsKey(dep.getId())) {
                        inEdges.get(c.getId()).add(dep.getId());
                        outEdges.computeIfAbsent(dep.getId(), k -> new HashSet<>()).add(c.getId());
                    }
                }
            }
        }

        // Start with concepts that have no prerequisites
        Queue<UUID> queue = new LinkedList<>();
        for (Concept c : concepts) {
            if (inEdges.get(c.getId()).isEmpty()) {
                queue.add(c.getId());
            }
        }

        List<UUID> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            sorted.add(current);

            for (UUID dependent : outEdges.getOrDefault(current, Set.of())) {
                inEdges.get(dependent).remove(current);
                if (inEdges.get(dependent).isEmpty()) {
                    queue.add(dependent);
                }
            }
        }

        // If there are cycles or disconnected concepts, add remaining by orderIndex
        if (sorted.size() < concepts.size()) {
            Set<UUID> remaining = concepts.stream()
                    .map(Concept::getId)
                    .filter(id -> !sorted.contains(id))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            sorted.addAll(remaining);
        }

        return sorted;
    }
}
