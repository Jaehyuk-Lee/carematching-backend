package com.sesac.carematching.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.chat.config.ApplicationInstance;
import com.sesac.carematching.chat.dto.MessageResponse;
import com.sesac.carematching.chat.pubsub.RedisPublisherService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageRedisService {
    private final RedisPublisherService redisPublisherService;
    private final ObjectMapper objectMapper;
    private final ApplicationInstance applicationInstance;

    // Redis로 브로드캐스트 (다른 인스턴스들에 전달)
    public void publishChatMessage(String roomId, MessageResponse message) {
        try {
            var payload = objectMapper.createObjectNode()
                .put("origin", applicationInstance.getInstanceId())
                .set("message", objectMapper.valueToTree(message));

            String json = objectMapper.writeValueAsString(payload);
            try {
                redisPublisherService.publish(getTopic(roomId), json);
            } catch (Exception e) {
                log.error("Redis publish 실패: roomId={}, message={}", roomId, message);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: {}", e.getMessage());
        }
    }

    public String getTopic(String roomId) {
        return "chat_room_" + roomId;
    }
}
