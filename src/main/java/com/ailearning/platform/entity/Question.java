package com.ailearning.platform.entity;

import com.ailearning.platform.entity.enums.QuestionType;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concept_id", nullable = false)
    private Concept concept;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "correct_answer")
    private String correctAnswer;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Builder.Default
    private Boolean aiGenerated = false;

    @Column(name = "generated_for_user_id")
    private UUID generatedForUserId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
