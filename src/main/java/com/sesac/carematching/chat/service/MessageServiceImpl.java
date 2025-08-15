package com.sesac.carematching.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.chat.dto.MessageRequest;
import com.sesac.carematching.chat.dto.MessageResponse;
import com.sesac.carematching.chat.message.Message;
import com.sesac.carematching.chat.message.MessageRepository;
import com.sesac.carematching.chat.pubsub.RedisPublisherService;
import com.sesac.carematching.chat.room.Room;
import com.sesac.carematching.chat.room.RoomRepository;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RedisPublisherService redisPublisherService;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    // DateTimeFormatter를 한 번만 생성해두고 재사용
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private static final String READ_KEY_FORMAT = "chat:read:%s:%s";

    @Override
    @Transactional
    public MessageResponse saveMessage(MessageRequest messageRequest) {
        // 1. 채팅방 조회 (MongoDB에서)
        Room room = roomRepository.findById(messageRequest.getRoomId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 ID입니다."));

        // 2. username을 사용하여 사용자 조회
        User user = userRepository.findByUsername(messageRequest.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 3. MongoDB에 메시지 저장
        Message message = new Message();
        message.setRoomId(room.getId());
        message.setUserId(user.getId());
        message.setUsername(user.getUsername());
        message.setMessage(messageRequest.getMessage());

        Message savedMessage = messageRepository.save(message);

        // 4. 생성시간을 각각 "MM/dd"와 "HH:mm" 형식으로 포맷팅
        String formattedDate = savedMessage.getCreatedAt()
            .atZone(ZoneId.systemDefault())
            .format(dateFormatter);
        String formattedTime = savedMessage.getCreatedAt()
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter);

        // 5. 저장된 메시지를 응답 DTO로 변환
        MessageResponse response = new MessageResponse(
            savedMessage.getRoomId(),
            savedMessage.getUsername(),
            savedMessage.getMessage(),
            savedMessage.getCreatedAt().toString(),
            formattedDate,
            formattedTime
        );
        // 6. Redis Pub/Sub 발행
        publishChatMessage(savedMessage.getRoomId(), response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesByRoomId(String roomId) {
        return messageRepository.findByRoomId(roomId).stream()
            .map(message -> {
                // 각 메시지마다 생성시간 포맷팅
                String formattedDate = message.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(dateFormatter);
                String formattedTime = message.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(timeFormatter);
                return new MessageResponse(
                    message.getRoomId(),
                    message.getUsername(),
                    message.getMessage(),
                    message.getCreatedAt().toString(),
                    formattedDate,
                    formattedTime
                );
            })
            .collect(Collectors.toList());
    }

    // Redis Pub/Sub 발행
    private void publishChatMessage(String roomId, MessageResponse message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisPublisherService.publish(getTopic(roomId), json);
        } catch (Exception e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }

    private String getTopic(String roomId) {
        return "chat_room_" + roomId;
    }

    // 사용자의 마지막 읽은 메시지 ID를 Redis에 저장
    private void setLastReadMessageId(String roomId, String userId, String messageId) {
        String key = String.format(READ_KEY_FORMAT, roomId, userId);
        redisTemplate.opsForValue().set(key, messageId);
    }

    // 사용자의 마지막 읽은 메시지 ID를 Redis에서 조회
    private String getLastReadMessageId(String roomId, String userId) {
        String key = String.format(READ_KEY_FORMAT, roomId, userId);
        return redisTemplate.opsForValue().get(key);
    }
}
