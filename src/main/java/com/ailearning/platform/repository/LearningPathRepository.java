package com.ailearning.platform.repository;

import com.ailearning.platform.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, UUID> {
    List<LearningPath> findByCourseId(UUID courseId);
    Optional<LearningPath> findByCourseIdAndIsDefaultTrue(UUID courseId);
}
