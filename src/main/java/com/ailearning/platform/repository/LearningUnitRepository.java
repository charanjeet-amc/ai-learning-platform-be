package com.ailearning.platform.repository;

import com.ailearning.platform.entity.LearningUnit;
import com.ailearning.platform.entity.enums.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearningUnitRepository extends JpaRepository<LearningUnit, UUID> {
    List<LearningUnit> findByConceptIdOrderByOrderIndexAsc(UUID conceptId);
    List<LearningUnit> findByConceptIdAndType(UUID conceptId, ContentType type);
}
