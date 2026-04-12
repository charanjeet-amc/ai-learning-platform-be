package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BadgeResponse {
    private UUID id;
    private String name;
    private String description;
    private String iconUrl;
    private Integer xpReward;
    private LocalDateTime earnedAt;
}
