package com.ailearning.platform.repository;

import com.ailearning.platform.entity.AIInteraction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AIInteractionRepository extends JpaRepository<AIInteraction, UUID> {
    List<AIInteraction> findByUserIdAndConceptIdOrderByCreatedAtDesc(UUID userId, UUID conceptId, Pageable pageable);
    List<AIInteraction> findByUserIdAndSessionIdOrderByCreatedAtAsc(UUID userId, String sessionId);
    List<AIInteraction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
