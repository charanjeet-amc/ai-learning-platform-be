package com.ailearning.platform.service.impl;

import com.ailearning.platform.dto.response.*;
import com.ailearning.platform.entity.*;
import com.ailearning.platform.entity.enums.ConceptStatus;
import com.ailearning.platform.exception.ResourceNotFoundException;
import com.ailearning.platform.repository.*;
import com.ailearning.platform.service.DashboardService;
import com.ailearning.platform.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserConceptProgressRepository progressRepository;
    private final GamificationService gamificationService;

    @Override
    public DashboardResponse getDashboard(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Enrollment> enrollments = enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId);

        List<EnrolledCourseResponse> enrolledCourses = enrollments.stream()
                .map(e -> EnrolledCourseResponse.builder()
                        .courseId(e.getCourse().getId())
                        .courseTitle(e.getCourse().getTitle())
                        .thumbnailUrl(e.getCourse().getThumbnailUrl())
                        .progressPercent(e.getProgressPercent())
                        .completed(e.getCompleted())
                        .enrolledAt(e.getEnrolledAt())
                        .build())
                .collect(Collectors.toList());

        List<UserProgressResponse> weakAreas = progressRepository
                .findWeakConcepts(userId, 0.6).stream()
                .limit(5)
                .map(p -> UserProgressResponse.builder()
                        .userId(userId)
                        .conceptId(p.getConcept().getId())
                        .conceptTitle(p.getConcept().getTitle())
                        .masteryLevel(p.getMasteryLevel())
                        .confidenceScore(p.getConfidenceScore())
                        .attempts(p.getAttempts())
                        .status(p.getStatus())
                        .build())
                .collect(Collectors.toList());

        List<BadgeResponse> badges = gamificationService.getUserBadges(userId);

        // Compute rank
        List<LeaderboardEntryResponse> leaderboard = gamificationService.getLeaderboard(100);
        int rank = leaderboard.stream()
                .filter(e -> e.getUserId().equals(userId))
                .findFirst()
                .map(LeaderboardEntryResponse::getRank)
                .orElse(0);

        return DashboardResponse.builder()
                .userId(userId)
                .fullName(user.getFullName())
                .totalXp(user.getTotalXp())
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .enrolledCourses(enrolledCourses)
                .recentBadges(badges.stream().limit(5).collect(Collectors.toList()))
                .weakAreas(weakAreas)
                .rank(rank)
                .build();
    }
}
