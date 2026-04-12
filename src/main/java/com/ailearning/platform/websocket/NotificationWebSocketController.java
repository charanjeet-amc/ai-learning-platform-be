package com.ailearning.platform.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/notification.read")
    public void markNotificationRead(@Payload Map<String, String> payload, Principal principal) {
        // Handle notification read acknowledgment
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/notifications",
                Map.of("type", "READ_ACK", "notificationId", payload.get("notificationId"))
        );
    }

    public void sendNotification(String userId, String title, String message, String type) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                Map.of("title", title, "message", message, "type", type)
        );
    }

    public void sendBadgeEarned(String userId, String badgeName) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                Map.of("type", "BADGE_EARNED", "badgeName", badgeName)
        );
    }

    public void sendXPUpdate(String userId, long totalXP, int xpGained) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                Map.of("type", "XP_UPDATE", "totalXP", totalXP, "xpGained", xpGained)
        );
    }
}
