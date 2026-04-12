package com.ailearning.platform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "media")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private LearningUnit learningUnit;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String type;

    private String fileName;

    private Long fileSizeBytes;

    private Integer durationSeconds;
}
