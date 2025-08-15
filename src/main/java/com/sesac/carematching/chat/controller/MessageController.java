package com.sesac.carematching.chat.controller;

import com.sesac.carematching.chat.dto.MessageRequest;
import com.sesac.carematching.chat.dto.MessageResponse;
import com.sesac.carematching.chat.service.ChatMessageService;
import com.sesac.carematching.chat.service.MessageService;
import com.sesac.carematching.config.ApiVersion;
import com.sesac.carematching.exception.VersionException;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Message Controller", description = "채팅 메시지 관리 및 송수신")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ChatMessageService chatMessageService;

    @Operation(summary = "채팅방 메시지 전체 조회", description = "특정 채팅방의 모든 메시지를 조회합니다.")
    @GetMapping("/{roomId}/{userId}")
    @ApiVersion(2)
    public List<MessageResponse> getMessagesByRoom(@PathVariable String roomId, @PathVariable String userId) {
        return messageService.getMessagesByRoomId(roomId, userId);
    }
    @GetMapping("/{roomId}")
    @ApiVersion(1)
    public void getMessagesByRoomVersionException() {
        throw new VersionException();
    }

    /**
     * 실시간 메시지 전송 및 저장
     */
    @MessageMapping("/chat/send")
    public void sendMessage(MessageRequest messageRequest) {
        System.out.println("[SEND] 메시지 요청: " + messageRequest);

        // 1. username으로 User ID 조회
        User user = userRepository.findByUsername(messageRequest.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 메시지 저장을 위해 MessageRequest를 업데이트
        messageRequest.setUserId(user.getId());

        // 3. 메시지 저장 및 송신
        MessageResponse savedMessage = messageService.saveMessage(messageRequest);
        // 기존 인스턴스에 메시지 전송
        messagingTemplate.convertAndSend("/topic/chat/" + messageRequest.getRoomId(), savedMessage);
        // 모든 인스턴스에 메시지 동기화 (Redis Pub/Sub)
        chatMessageService.publishChatMessage(messageRequest.getRoomId(), savedMessage);
    }

    @PostMapping("/{roomId}/{userId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String roomId, @PathVariable String userId, @RequestBody String lastReadMessageId) {
        messageService.markAsRead(roomId, userId, lastReadMessageId);
        return ResponseEntity.ok().build();
    }
}
