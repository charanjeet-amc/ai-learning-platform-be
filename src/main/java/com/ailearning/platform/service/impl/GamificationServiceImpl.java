package com.ailearning.platform.service.impl;

import com.ailearning.platform.dto.response.BadgeResponse;
import com.ailearning.platform.dto.response.LeaderboardEntryResponse;
import com.ailearning.platform.entity.*;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.repository.*;
import com.ailearning.platform.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationServiceImpl implements GamificationService {

    private final XPEventRepository xpEventRepository;
    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;

    @Override
    @Transactional
    public void awardXP(UUID userId, String eventType, int amount, UUID referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        XPEvent event = XPEvent.builder()
                .user(user)
                .eventType(eventType)
                .xpAmount(amount)
                .referenceId(referenceId)
                .build();
        xpEventRepository.save(event);

        user.setTotalXp(user.getTotalXp() + amount);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void checkAndAwardBadges(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        long totalXp = user.getTotalXp();

        // XP milestone badges
        checkAndAwardBadge(user, "First Steps", totalXp >= 100);
        checkAndAwardBadge(user, "Rising Star", totalXp >= 1000);
        checkAndAwardBadge(user, "Knowledge Seeker", totalXp >= 5000);
        checkAndAwardBadge(user, "Master Learner", totalXp >= 10000);

        // Streak badges
        checkAndAwardBadge(user, "Consistent Learner", user.getCurrentStreak() >= 7);
        checkAndAwardBadge(user, "Dedicated Scholar", user.getCurrentStreak() >= 30);
    }

    @Override
    @Transactional
    public void updateStreak(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        LocalDateTime lastActive = user.getLastActiveAt();
        LocalDateTime now = LocalDateTime.now();

        if (lastActive == null || lastActive.toLocalDate().isBefore(now.toLocalDate().minusDays(1))) {
            user.setCurrentStreak(1);
        } else if (lastActive.toLocalDate().isBefore(now.toLocalDate())) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        }

        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }

        user.setLastActiveAt(now);
        userRepository.save(user);
    }

    @Override
    public List<BadgeResponse> getUserBadges(UUID userId) {
        return userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId).stream()
                .map(ub -> BadgeResponse.builder()
                        .id(ub.getBadge().getId())
                        .name(ub.getBadge().getName())
                        .description(ub.getBadge().getDescription())
                        .iconUrl(ub.getBadge().getIconUrl())
                        .xpReward(ub.getBadge().getXpReward())
                        .earnedAt(ub.getEarnedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaderboardEntryResponse> getLeaderboard(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "totalXp"));
        List<User> topUsers = userRepository.findAll(pageRequest).getContent();

        AtomicInteger rank = new AtomicInteger(1);
        return topUsers.stream()
                .map(user -> LeaderboardEntryResponse.builder()
                        .rank(rank.getAndIncrement())
                        .userId(user.getId())
                        .fullName(user.getFullName())
                        .avatarUrl(user.getAvatarUrl())
                        .totalXp(user.getTotalXp())
                        .currentStreak(user.getCurrentStreak())
                        .badgeCount(userBadgeRepository.findByUserIdOrderByEarnedAtDesc(user.getId()).size())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public long getUserXP(UUID userId) {
        return xpEventRepository.sumXpByUserId(userId);
    }

    private void checkAndAwardBadge(User user, String badgeName, boolean condition) {
        if (!condition) return;
        badgeRepository.findByName(badgeName).ifPresent(badge -> {
            if (!userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
                UserBadge userBadge = UserBadge.builder()
                        .user(user)
                        .badge(badge)
                        .build();
                userBadgeRepository.save(userBadge);

                user.setTotalXp(user.getTotalXp() + badge.getXpReward());
                userRepository.save(user);
            }
        });
    }
}
