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

    @Column(name = "content_type", nullable = false)
    private String type;

    @Column(name = "filename")
    private String fileName;

    @Column(name = "size_bytes")
    private Long fileSizeBytes;

    private Integer durationSeconds;
}
