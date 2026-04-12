package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DashboardResponse {
    private UUID userId;
    private String fullName;
    private Long totalXp;
    private Integer currentStreak;
    private Integer longestStreak;
    private List<EnrolledCourseResponse> enrolledCourses;
    private List<BadgeResponse> recentBadges;
    private List<UserProgressResponse> weakAreas;
    private Integer rank;
}

