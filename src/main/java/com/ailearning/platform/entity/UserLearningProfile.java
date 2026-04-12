package com.ailearning.platform.entity;

import com.ailearning.platform.entity.enums.LearningStyle;
import com.ailearning.platform.entity.enums.LearningPace;
import com.ailearning.platform.entity.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_learning_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserLearningProfile {

    @Id
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LearningStyle preferredStyle = LearningStyle.TEXT;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LearningPace pace = LearningPace.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_preference")
    @Builder.Default
    private DifficultyLevel skillLevel = DifficultyLevel.BEGINNER;

    @Builder.Default
    private Integer visualScore = 0;

    @Builder.Default
    private Integer codeScore = 0;

    @Builder.Default
    private Integer textScore = 0;

    @Builder.Default
    private Integer auditoryScore = 0;

    private String goals;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
