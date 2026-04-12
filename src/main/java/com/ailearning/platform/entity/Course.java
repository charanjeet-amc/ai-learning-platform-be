package com.ailearning.platform.entity;

import com.ailearning.platform.entity.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DifficultyLevel difficulty = DifficultyLevel.BEGINNER;

    private Integer estimatedDurationMinutes;

    private String industryVertical;

    @Column(columnDefinition = "TEXT[]")
    private String[] skillsOutcome;

    @Column(columnDefinition = "TEXT[]")
    private String[] prerequisites;

    @Column(columnDefinition = "TEXT[]")
    private String[] tags;

    @Builder.Default
    private Boolean published = false;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Long enrollmentCount = 0L;

    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Module> modules = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LearningPath> learningPaths = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
