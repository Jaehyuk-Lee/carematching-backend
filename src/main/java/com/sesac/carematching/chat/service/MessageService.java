package com.sesac.carematching.chat.service;

import com.sesac.carematching.chat.dto.MessageRequest;
import com.sesac.carematching.chat.dto.MessageResponse;

import java.util.List;

public interface MessageService {
    MessageResponse saveMessage(MessageRequest messageRequest);
    List<MessageResponse> getMessagesByRoomId(Integer roomId);
}
