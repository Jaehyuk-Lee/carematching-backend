package com.sesac.carematching.chat.service;

import com.sesac.carematching.chat.dto.MessageRequest;
import com.sesac.carematching.chat.dto.MessageResponse;

import java.util.List;

public interface MessageService<T> {

    MessageResponse<T> saveMessage(MessageRequest<T> messageRequest);

    List<MessageResponse<T>> getMessagesByRoomId(T roomId);
}
