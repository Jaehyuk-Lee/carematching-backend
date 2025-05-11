package com.sesac.carematching.chat.service;

import com.sesac.carematching.chat.dto.MessageRequest;
import com.sesac.carematching.chat.dto.MessageResponse;
import com.sesac.carematching.chat.message.MongoMessage;
import com.sesac.carematching.chat.message.MongoMessageRepository;
import com.sesac.carematching.chat.room.MongoRoom;
import com.sesac.carematching.chat.room.MongoRoomRepository;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MongoMessageRepository mongoMessageRepository;
    private final MongoRoomRepository mongoRoomRepository;
    private final UserRepository userRepository;

    // DateTimeFormatter를 한 번만 생성해두고 재사용
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional
    public MessageResponse saveMessage(MessageRequest messageRequest) {
        // 1. 채팅방 조회 (MongoDB에서)
        MongoRoom room = mongoRoomRepository.findById(messageRequest.getRoomId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 ID입니다."));

        // 2. username을 사용하여 사용자 조회
        User user = userRepository.findByUsername(messageRequest.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 3. MongoDB에 메시지 저장
        MongoMessage mongoMessage = new MongoMessage();
        mongoMessage.setRoomId(room.getId());
        mongoMessage.setUserId(user.getId());
        mongoMessage.setUsername(user.getUsername());
        mongoMessage.setMessage(messageRequest.getMessage());

        MongoMessage savedMessage = mongoMessageRepository.save(mongoMessage);

        // 4. 생성시간을 각각 "MM/dd"와 "HH:mm" 형식으로 포맷팅
        String formattedDate = savedMessage.getCreatedAt()
            .atZone(ZoneId.systemDefault())
            .format(dateFormatter);
        String formattedTime = savedMessage.getCreatedAt()
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter);

        // 5. 저장된 메시지를 응답 DTO로 변환
        return new MessageResponse(
            savedMessage.getRoomId(),
            savedMessage.getUsername(),
            savedMessage.getMessage(),
            savedMessage.getCreatedAt().toString(),
            formattedDate,
            formattedTime
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesByRoomId(String roomId) {
        return mongoMessageRepository.findByRoomId(roomId).stream()
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
}
