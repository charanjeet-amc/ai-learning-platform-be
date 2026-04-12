package com.ailearning.platform.controller;

import com.ailearning.platform.dto.response.BadgeResponse;
import com.ailearning.platform.dto.response.LeaderboardEntryResponse;
import com.ailearning.platform.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping("/badges")
    public ResponseEntity<List<BadgeResponse>> getMyBadges(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(gamificationService.getUserBadges(userId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(gamificationService.getLeaderboard(Math.min(limit, 100)));
    }

    @GetMapping("/xp")
    public ResponseEntity<Long> getMyXP(
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(gamificationService.getUserXP(userId));
    }
}
