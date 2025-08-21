package com.sesac.carematching.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.chat.config.ApplicationInstance;
import com.sesac.carematching.chat.pubsub.RedisPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final RedisPublisherService redisPublisherService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationInstance applicationInstance;

    public void sendNotificationToUser(String username, String message) {
        // 1) 로컬으로 즉시 전달 (자기 자신 처리 보장)
        try {
            messagingTemplate.convertAndSend("/queue/notifications/" + username, message);
        } catch (Exception e) {
            log.warn("로컬 WebSocket 전송 실패 ({}): {}", username, e.getMessage());
        }

        // 2) Redis로 브로드캐스트 (다른 인스턴스들에 전달)
        try {
            var payload = objectMapper.createObjectNode()
                .put("origin", applicationInstance.getInstanceId())
                .put("username", username)
                .put("text", message);

            String payloadStr = objectMapper.writeValueAsString(payload);
            try {
                redisPublisherService.publish("chat_notifications", payloadStr);
            }
            catch (Exception e) {
                log.error("Redis publish 실패: username={}, message={}", username, message);
            }
            log.info("Redis 알림 전송 → {}: {}", username, message);
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: {}", e.getMessage());
        }
    }
}




