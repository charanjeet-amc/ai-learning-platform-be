package com.ailearning.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "xp_events")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class XPEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reason", nullable = false)
    private String eventType;

    @Column(name = "amount", nullable = false)
    private Integer xpAmount;

    @Column(name = "source_id")
    private UUID referenceId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
