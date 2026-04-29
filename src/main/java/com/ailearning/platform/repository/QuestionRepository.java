package com.ailearning.platform.repository;

import com.ailearning.platform.entity.Question;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import com.ailearning.platform.entity.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Query("SELECT q FROM Question q WHERE q.concept.id = :conceptId " +
           "AND (q.generatedForUserId IS NULL OR q.generatedForUserId = :userId)")
    List<Question> findByConceptIdForUser(@Param("conceptId") UUID conceptId, @Param("userId") UUID userId);

    List<Question> findByConceptId(UUID conceptId);
    List<Question> findByConceptIdAndType(UUID conceptId, QuestionType type);
    List<Question> findByConceptIdAndDifficulty(UUID conceptId, DifficultyLevel difficulty);
}
