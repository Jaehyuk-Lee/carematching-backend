package com.sesac.carematching.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChannelTopic topic;
    private String message;

    // ✅ Redis를 통해 알림 메시지 전송 (username 기반)
    // ✅ Redis를 통해 알림 메시지 전송 (username 기반)
        public void sendNotificationToUser(String username, String message) {
        log.info("📢 Redis 알림 전송 → {}: {}", username, message);
        redisTemplate.convertAndSend(topic.getTopic(), username + ":" + message);
    }


    // ✅ Redis에서 수신한 메시지를 WebSocket으로 전송
    public void onMessage(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length < 2) return;

        String username = parts[0]; // 🔥 userId → username 변경
        String notification = parts[1];

        log.info("📩 Redis 알림 수신 → {}: {}", username, notification);

        // WebSocket을 통해 클라이언트에게 알림 전송
        messagingTemplate.convertAndSend("/queue/notifications/" + username, notification);
    }
}




