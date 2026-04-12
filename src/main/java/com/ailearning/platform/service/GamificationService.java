package com.ailearning.platform.service;

import com.ailearning.platform.dto.response.BadgeResponse;
import com.ailearning.platform.dto.response.LeaderboardEntryResponse;

import java.util.List;
import java.util.UUID;

public interface GamificationService {
    void awardXP(UUID userId, String eventType, int amount, UUID referenceId);
    void checkAndAwardBadges(UUID userId);
    void updateStreak(UUID userId);
    List<BadgeResponse> getUserBadges(UUID userId);
    List<LeaderboardEntryResponse> getLeaderboard(int limit);
    long getUserXP(UUID userId);
}
