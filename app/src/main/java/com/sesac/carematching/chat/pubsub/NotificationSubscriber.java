package com.sesac.carematching.chat.pubsub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.chat.config.ApplicationInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationInstance applicationInstance;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(body);

            // 자신이 보낸 메시지면 무시
            String origin = root.path("origin").asText(null);
            if (PubSubUtils.isOwnOrigin(origin, applicationInstance.getInstanceId())) return;

            String username = root.path("username").asText(null);
            String notification = root.path("text").asText(null);

            log.info("Redis 알림 수신 body → {}", body);

            if (username == null || notification == null) {
                log.warn("[NotificationSubscriber] 잘못된 포맷: {}", body);
                return;
            }

            // WebSocket을 통해 프론트엔드로 전달
            messagingTemplate.convertAndSend("/queue/notifications/" + username, notification);
        } catch (Exception e) {
            log.error("[NotificationSubscriber] 메시지 처리 실패", e);
        }
    }
}


