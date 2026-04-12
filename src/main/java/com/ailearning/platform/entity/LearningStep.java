package com.ailearning.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "learning_steps")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LearningStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "path_id", nullable = false)
    private LearningPath learningPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concept_id", nullable = false)
    private Concept concept;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Builder.Default
    private Boolean mandatory = true;
}
