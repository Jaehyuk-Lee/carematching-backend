/*package com.sesac.carematching.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    // 전체 사용자에게 알림 전송
    @MessageMapping("/send-notification")
    @SendTo("/topic/notifications")
    public String sendNotification(String message) {
        log.info("Sending notification: {}", message);
        return message; // 모든 구독자에게 전달
    }

    // 특정 사용자에게 알림 전송
    public void sendNotificationToUser(String userId, String message) {
        String destination = "/queue/notifications/" + userId;
        log.info("Sending notification to {}: {}", userId, message);
        messagingTemplate.convertAndSend(destination, message);
    }
}*/
