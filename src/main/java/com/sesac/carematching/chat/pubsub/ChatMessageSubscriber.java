package com.sesac.carematching.chat.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.chat.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatMessageSubscriber {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // Redis에서 채팅 메시지 수신
    public void onMessage(String message, String channel) {
        try {
            MessageResponse msg = objectMapper.readValue(message, MessageResponse.class);
            // 채널명에서 roomId 추출 (예: chat_room_{roomId})
            String roomId = channel.substring("chat_room_".length());
            // WebSocket으로 해당 채팅방 구독자에게 메시지 전달
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, msg);
        } catch (Exception e) {
            log.error("[ChatMessageSubscriber] 메시지 파싱 실패", e);
        }
    }
}
