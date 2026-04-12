package com.ailearning.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_interactions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AIInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concept_id")
    private Concept concept;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String query;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String response;

    private String sessionId;

    @Builder.Default
    private Integer usefulnessScore = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
