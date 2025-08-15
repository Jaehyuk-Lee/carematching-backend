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
    public List<MessageResponse> getMessagesByRoomId(String roomId, String userId) {
        // 사용자의 마지막 읽은 메시지 ID 조회
        String lastReadMessageId = getLastReadMessageId(roomId, userId);
        return messageRepository.findByRoomId(roomId).stream()
            .map(message -> {
                String formattedDate = message.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(dateFormatter);
                String formattedTime = message.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(timeFormatter);
                boolean isRead = false;
                if (lastReadMessageId != null) {
                    // lastReadMessageId는 MongoDB의 message id(ObjectId 문자열)로 저장될 수 있으므로,
                    // 안전하게 createdAt(Instant) 기준으로 비교합니다. Redis에는 message id가 아닌 createdAt epochMillis를 저장하는
                    // 방식으로 변경하는 것이 더 안전하지만, 현재는 lastReadMessageId가 메시지 id라면 MongoDB에서 해당 메시지를 조회하여
                    // 그 메시지의 createdAt을 사용하여 비교할 수 있습니다. 간단하게는 문자열 비교를 피하기 위해 try-catch를 사용합니다.
                    try {
                        // Redis에 저장된 값이 epochMillis(숫자)인 경우
                        long lastReadEpoch = Long.parseLong(lastReadMessageId);
                        long messageEpoch = message.getCreatedAt().toEpochMilli();
                        isRead = messageEpoch <= lastReadEpoch;
                    } catch (NumberFormatException e) {
                        // 저장된 값이 ObjectId 문자열일 경우, 비교 불가이므로 false로 처리
                        isRead = false;
                    }
                }
                return new MessageResponse(
                    message.getRoomId(),
                    message.getUsername(),
                    message.getMessage(),
                    message.getCreatedAt().toString(),
                    formattedDate,
                    formattedTime,
                    isRead
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
    // 단, 전달된 값이 epochMillis(숫자)라면 기존 값보다 클 때만 갱신합니다.
    private void setLastReadMessageId(String roomId, String userId, String messageId) {
        String key = String.format(READ_KEY_FORMAT, roomId, userId);
        if (messageId == null) return;
        try {
            long newEpoch = Long.parseLong(messageId);
            String current = redisTemplate.opsForValue().get(key);
            if (current == null) {
                redisTemplate.opsForValue().set(key, String.valueOf(newEpoch));
                return;
            }
            try {
                long currentEpoch = Long.parseLong(current);
                if (newEpoch > currentEpoch) {
                    redisTemplate.opsForValue().set(key, String.valueOf(newEpoch));
                }
                return;
            } catch (NumberFormatException ex) {
                // 현재 값이 숫자가 아니면 덮어쓰기
                redisTemplate.opsForValue().set(key, String.valueOf(newEpoch));
                return;
            }
        } catch (NumberFormatException e) {
            // 전달된 값이 숫자가 아닐 경우(예: 메시지 id 문자열), 그대로 저장
            redisTemplate.opsForValue().set(key, messageId);
        }
    }

    // 사용자의 마지막 읽은 메시지 ID를 Redis에서 조회
    private String getLastReadMessageId(String roomId, String userId) {
        String key = String.format(READ_KEY_FORMAT, roomId, userId);
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void markAsRead(String roomId, String userId, String lastReadMessageId) {
        // lastReadMessageId가 MongoDB 메시지 id일 수 있으므로, 해당 메시지를 찾아 createdAt epochMillis를 Redis에 저장
        try {
            // 먼저 메시지 id를 기준으로 MongoDB에서 조회 시도
            Message msg = messageRepository.findById(lastReadMessageId).orElse(null);
            if (msg != null) {
                long epochMillis = msg.getCreatedAt().toEpochMilli();
                String key = String.format(READ_KEY_FORMAT, roomId, userId);
                redisTemplate.opsForValue().set(key, String.valueOf(epochMillis));
                return;
            }
        } catch (Exception ignored) {
            // 조회 실패 시에도 다음 단계로 넘어감
        }

        // 메시지 id로 조회되지 않는 경우(lastReadMessageId가 epochMillis 문자열일 수 있음), 그대로 저장
        String key = String.format(READ_KEY_FORMAT, roomId, userId);
        redisTemplate.opsForValue().set(key, lastReadMessageId);
    }
}
