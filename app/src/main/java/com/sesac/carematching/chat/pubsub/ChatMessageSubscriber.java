package com.sesac.carematching.chat.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.chat.config.ApplicationInstance;
import com.sesac.carematching.chat.dto.MessageResponse;
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
public class ChatMessageSubscriber implements MessageListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationInstance applicationInstance;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(body);

            // 자신이 보낸 메시지면 무시
            String origin = root.path("origin").asText(null);
            if (PubSubUtils.isOwnOrigin(origin, applicationInstance.getInstanceId())) return;

            JsonNode msgNode = root.path("message");
            String msg = objectMapper.treeToValue(msgNode, String.class);

            // 채널명에서 roomId 추출 (예: chat_room_{roomId})
            String roomId = channel.substring("chat_room_".length());

            // WebSocket을 통해 프론트엔드로 전달
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, msg);
        } catch (JsonProcessingException e) {
            log.error("[ChatMessageSubscriber] 메시지 파싱 실패", e);
        }
    }
}
