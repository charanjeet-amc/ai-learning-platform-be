package com.ailearning.platform.repository;

import com.ailearning.platform.entity.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConceptRepository extends JpaRepository<Concept, UUID> {
    List<Concept> findByTopicIdOrderByOrderIndexAsc(UUID topicId);

    @Query("SELECT c FROM Concept c JOIN c.dependencies d WHERE d.id = :conceptId")
    List<Concept> findDependentsOf(@Param("conceptId") UUID conceptId);

    @Query("SELECT c.dependencies FROM Concept c WHERE c.id = :conceptId")
    List<Concept> findPrerequisites(@Param("conceptId") UUID conceptId);
}
