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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
        // 상대방(파트너)의 username을 찾아 그 사용자의 마지막 읽음 epoch으로 메시지의 read 여부를 판단
        String partnerUsername = null;
        try {
            com.sesac.carematching.chat.room.Room room = roomRepository.findById(roomId).orElse(null);
            if (room != null) {
                if (room.getRequesterUsername().equals(userId)) {
                    partnerUsername = room.getReceiverUsername();
                } else {
                    partnerUsername = room.getRequesterUsername();
                }
            }
        } catch (Exception ignored) {
        }

        String partnerLastRead = null;
        if (partnerUsername != null) {
            partnerLastRead = getLastReadMessageId(roomId, partnerUsername);
        }
        final String partnerLastReadFinal = partnerLastRead;
        final String partnerUsernameFinal = partnerUsername;

        return messageRepository.findByRoomId(roomId).stream()
            .map(message -> {
                String formattedDate = message.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(dateFormatter);
                String formattedTime = message.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(timeFormatter);
                boolean isRead = false;
                if (partnerLastReadFinal != null) {
                    try {
                        long lastReadEpoch = Long.parseLong(partnerLastReadFinal);
                        long messageEpoch = message.getCreatedAt().toEpochMilli();
                        isRead = messageEpoch <= lastReadEpoch;
                    } catch (NumberFormatException e) {
                        // partner의 lastRead가 비정상 포맷이면 false
                        log.warn("Invalid partner lastRead for room {} partner {}: {}", roomId, partnerUsernameFinal, partnerLastReadFinal);
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

    // 사용자의 마지막 읽은 메시지(epochMillis)만을 Redis에 저장
    // 전달된 값이 숫자가 아닌 경우는 무시
    private void setLastReadMessageId(String roomId, String userId, String messageId) {
        String key = String.format(READ_KEY_FORMAT, roomId, userId);
        if (messageId == null) return;
        try {
            long newEpoch = Long.parseLong(messageId);
            String current = redisTemplate.opsForValue().get(key);
            if (current == null) {
                redisTemplate.opsForValue().set(key, String.valueOf(newEpoch));
                // ZSET에 업데이트 기록
                redisTemplate.opsForZSet().add("chat:read:updated", roomId + ":" + userId, newEpoch);
                return;
            }
            try {
                long currentEpoch = Long.parseLong(current);
                if (newEpoch > currentEpoch) {
                    redisTemplate.opsForValue().set(key, String.valueOf(newEpoch));
                    // ZSET에 업데이트 기록
                    redisTemplate.opsForZSet().add("chat:read:updated", roomId + ":" + userId, newEpoch);
                }
                return;
            } catch (NumberFormatException ex) {
                // 현재 값이 숫자가 아닌 경우(비정상 값)에는 덮어쓰기 하지 않고 무시
                log.warn("Current lastRead for key {} is not numeric: {}", key, current);
                return;
            }
        } catch (NumberFormatException e) {
            // 전달된 값이 숫자가 아닐 경우: 하위호환성 미지원이므로 저장하지 않음
            log.warn("Ignoring non-numeric lastRead value for room {} user {}: {}", roomId, userId, messageId);
            return;
        }
    }

    // 사용자의 마지막 읽은 메시지 ID를 Redis에서 조회
    private String getLastReadMessageId(String roomId, String userId) {
        String key = String.format(READ_KEY_FORMAT, roomId, userId);
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void markAsRead(String roomId, String userId, Long lastReadEpochMillis) {
        if (lastReadEpochMillis == null) return;
        String key = String.format(READ_KEY_FORMAT, roomId, userId);
        String current = redisTemplate.opsForValue().get(key);
        try {
            long currentEpoch = current == null ? 0L : Long.parseLong(current);
            if (lastReadEpochMillis > currentEpoch) {
                redisTemplate.opsForValue().set(key, String.valueOf(lastReadEpochMillis));
                redisTemplate.opsForZSet().add("chat:read:updated", roomId + ":" + userId, lastReadEpochMillis);
            }
        } catch (NumberFormatException e) {
            // 기존 값이 숫자가 아닌 경우 덮어쓰기
            redisTemplate.opsForValue().set(key, String.valueOf(lastReadEpochMillis));
            redisTemplate.opsForZSet().add("chat:read:updated", roomId + ":" + userId, lastReadEpochMillis);
        }
    }
}
