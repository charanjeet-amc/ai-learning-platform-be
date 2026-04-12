package com.ailearning.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_attempts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> answer;

    @Builder.Default
    private Double score = 0.0;

    @Builder.Default
    private Boolean correct = false;

    private Long timeTakenSeconds;

    @Builder.Default
    private Integer hintsUsed = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
