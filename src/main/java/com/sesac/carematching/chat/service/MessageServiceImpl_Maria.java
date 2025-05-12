package com.sesac.carematching.chat.service;

import com.sesac.carematching.chat.dto.MessageRequest;
import com.sesac.carematching.chat.dto.MessageResponse;
import com.sesac.carematching.chat.message.Message;
import com.sesac.carematching.chat.message.MessageRepository;
import com.sesac.carematching.chat.room.Room;
import com.sesac.carematching.chat.room.RoomRepository;
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
public class MessageServiceImpl_Maria implements MessageService<Integer> {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    // ✅ DateTimeFormatter를 한 번만 생성해두고 재사용
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional
    public MessageResponse<Integer> saveMessage(MessageRequest<Integer> messageRequest) {
        // ✅ 1. 채팅방 조회
        Room room = roomRepository.findById(messageRequest.getRoomId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방 ID입니다."));

        // ✅ 2. username을 사용하여 사용자 조회
        User user = userRepository.findByUsername(messageRequest.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // ✅ 3. 메시지 저장
        Message message = new Message();
        message.setRoom(room);
        message.setUser(user);
        message.setMessage(messageRequest.getMessage());
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);

        // ✅ 4. 생성시간을 각각 "MM/dd"와 "HH:mm" 형식으로 포맷팅
        return convertToMessageResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse<Integer>> getMessagesByRoomId(Integer roomId) {
        return messageRepository.findByRoomId(roomId).stream()
            .map(this::convertToMessageResponse)
            .collect(Collectors.toList());
    }

    private MessageResponse<Integer> convertToMessageResponse(Message message) {
        // ✅ 각 메시지마다 생성시간 포맷팅
        String formattedDate = message.getCreatedAt()
            .atZone(ZoneId.systemDefault())
            .format(dateFormatter);
        String formattedTime = message.getCreatedAt()
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter);
        return new MessageResponse<>(
            message.getRoom().getId(),
            message.getUser().getUsername(), // ✅ userId 대신 username 사용
            message.getMessage(),
            message.getCreatedAt().toString(),
            formattedDate,
            formattedTime
        );
    }
}
