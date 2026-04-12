package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LeaderboardEntryResponse {
    private Integer rank;
    private UUID userId;
    private String fullName;
    private String avatarUrl;
    private Long totalXp;
    private Integer currentStreak;
    private Integer badgeCount;
}
