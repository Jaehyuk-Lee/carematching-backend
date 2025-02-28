package com.sesac.carematching.chat.controller;

import com.sesac.carematching.chat.dto.MessageRequest;
import com.sesac.carematching.chat.dto.MessageResponse;
import com.sesac.carematching.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @MessageMapping("/chat/send")
    @SendTo("/topic/chat/{roomId}")
    public MessageResponse sendMessage(MessageRequest messageRequest) {
        return messageService.saveMessage(messageRequest);
    }

    @GetMapping("/room/{roomId}")
    public List<MessageResponse> getMessages(@PathVariable Integer roomId) {
        return messageService.getMessagesByRoomId(roomId);
    }
}
