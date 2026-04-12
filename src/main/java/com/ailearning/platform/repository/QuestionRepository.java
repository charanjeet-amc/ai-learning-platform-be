package com.ailearning.platform.repository;

import com.ailearning.platform.entity.Question;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import com.ailearning.platform.entity.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByConceptId(UUID conceptId);
    List<Question> findByConceptIdAndType(UUID conceptId, QuestionType type);
    List<Question> findByConceptIdAndDifficulty(UUID conceptId, DifficultyLevel difficulty);
}
