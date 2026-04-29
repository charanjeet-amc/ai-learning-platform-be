package com.ailearning.platform.entity;

import com.ailearning.platform.entity.enums.ConceptStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_concept_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "concept_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserConceptProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concept_id", nullable = false)
    private Concept concept;

    @Column(name = "mastery_score")
    @Builder.Default
    private Double masteryLevel = 0.0;

    @Column(name = "confidence")
    @Builder.Default
    private Double confidenceScore = 1.0;

    @Builder.Default
    private Integer attempts = 0;

    @Builder.Default
    private Integer correctAttempts = 0;

    @Builder.Default
    private Integer hintsUsed = 0;

    @Column(name = "time_spent_minutes")
    @Builder.Default
    private Long timeSpentSeconds = 0L;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConceptStatus status = ConceptStatus.LOCKED;

    @Builder.Default
    private Boolean fastTracked = false;

    private LocalDateTime lastAccessedAt;

    private LocalDateTime nextReviewAt;

    @Builder.Default
    private Integer reviewIntervalDays = 1;

    @Builder.Default
    private Double easeFactor = 2.5;

    @Builder.Default
    private Double frustrationScore = 0.0;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
