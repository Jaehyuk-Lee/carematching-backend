package com.sesac.carematching.chat.service;

import com.sesac.carematching.chat.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.chat.pubsub.RedisPublisherService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final RedisPublisherService redisPublisherService;
    private final ObjectMapper objectMapper;

    // 채팅 메시지 Redis Pub/Sub 발행
    public void publishChatMessage(String roomId, MessageResponse message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisPublisherService.publish(getTopic(roomId), json);
        } catch (Exception e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }

    public String getTopic(String roomId) {
        return "chat_room_" + roomId;
    }
}
