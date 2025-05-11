package com.sesac.carematching.chat.controller;

import com.sesac.carematching.chat.dto.MessageRequest;
import com.sesac.carematching.chat.dto.MessageResponse;
import com.sesac.carematching.chat.service.MessageService;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository; // ✅ UserRepository 주입

    /**
     * 채팅방의 모든 메시지 불러오기
     */
    @GetMapping("/{roomId}")
    public List<MessageResponse> getMessagesByRoom(@PathVariable String roomId) {
        return messageService.getMessagesByRoomId(roomId);
    }

    /**
     * 실시간 메시지 전송 및 저장
     */
    @MessageMapping("/chat/send")
    public void sendMessage(MessageRequest messageRequest) {
        System.out.println("📤 [SEND] 메시지 요청: " + messageRequest);

        // 1. username으로 User ID 조회
        User user = userRepository.findByUsername(messageRequest.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 메시지 저장을 위해 MessageRequest를 업데이트
        messageRequest.setUserId(user.getId());

        // 3. 메시지 저장 및 송신
        MessageResponse savedMessage = messageService.saveMessage(messageRequest);
        messagingTemplate.convertAndSend("/topic/chat/" + messageRequest.getRoomId(), savedMessage);
    }
}
