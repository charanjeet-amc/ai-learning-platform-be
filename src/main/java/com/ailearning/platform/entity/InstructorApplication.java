package com.ailearning.platform.entity;

import com.ailearning.platform.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instructor_applications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class InstructorApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Professional info
    @Column(columnDefinition = "TEXT")
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String cvUrl;

    private String linkedinUrl;
    private String githubUrl;
    private String websiteUrl;

    // Teaching experience
    private Integer yearsTeaching;

    @Column(columnDefinition = "TEXT")
    private String currentInstitution;

    @Column(columnDefinition = "TEXT")
    private String teachingDescription;

    // Online presence
    private String youtubeChannelUrl;
    private Integer youtubeSubscribers;

    @Column(columnDefinition = "TEXT")
    private String otherPlatforms;

    // Subject expertise
    @Column(columnDefinition = "TEXT")
    private String expertise;

    @Column(columnDefinition = "TEXT")
    private String whyTeach;

    // Application status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    private UUID reviewedBy;

    private LocalDateTime reviewedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
