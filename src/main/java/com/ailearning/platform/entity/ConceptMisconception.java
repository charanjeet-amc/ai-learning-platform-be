package com.ailearning.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "concept_misconceptions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ConceptMisconception {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concept_id", nullable = false)
    private Concept concept;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String misconception;
}
