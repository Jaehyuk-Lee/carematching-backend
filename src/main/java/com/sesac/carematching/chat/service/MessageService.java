package com.sesac.carematching.chat.service;

import com.sesac.carematching.chat.dto.MessageRequest;
import com.sesac.carematching.chat.dto.MessageResponse;

import java.util.List;

public interface MessageService {

    MessageResponse saveMessage(MessageRequest messageRequest);

    List<MessageResponse> getMessagesByRoomId(String roomId, String userId);

    /**
     * 사용자가 특정 메시지까지 읽었다고 표시(마지막 읽음 메시지 ID 전달).
     * 구현은 Redis에 해당 메시지의 createdAt epochMillis를 저장합니다.
     */
    void markAsRead(String roomId, String userId, Long lastReadEpochMillis);
}
