/*
package com.sesac.carematching.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChannelTopic topic;

    // ✅ Redis를 통해 알림 메시지 전송
    public void sendNotificationToUser(String userId, String message) {
        log.info("📢 Redis로 알림 전송: {}", message);
        redisTemplate.convertAndSend(topic.getTopic(), userId + ":" + message);
    }

    // ✅ Redis에서 수신한 메시지를 WebSocket으로 전송
    public void onMessage(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length < 2) return;

        String userId = parts[0];
        String notification = parts[1];

        log.info("📩 Redis 알림 수신 - 사용자 {}: {}", userId, notification);

        // WebSocket을 통해 클라이언트에게 알림 전송
        messagingTemplate.convertAndSend("/queue/notifications/" + userId, notification);
    }
}
*/
