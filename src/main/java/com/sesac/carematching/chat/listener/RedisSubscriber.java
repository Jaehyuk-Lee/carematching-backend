package com.sesac.carematching.chat.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisSubscriber {

    private final SimpMessagingTemplate messagingTemplate;

    public void onMessage(String message) {
        log.info("📩 Redis 알림 수신: {}", message); // 👉 여기 로그 확인

        String[] parts = message.split(":", 2);
        if (parts.length < 2) return;

        String userId = parts[0];
        String notification = parts[1];

        // WebSocket을 통해 프론트엔드로 전달
        messagingTemplate.convertAndSend("/queue/notifications/" + userId, notification);
    }
}


