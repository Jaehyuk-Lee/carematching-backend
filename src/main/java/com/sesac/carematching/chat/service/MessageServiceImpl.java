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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Override
    public MessageResponse saveMessage(MessageRequest messageRequest) {
        Room room = roomRepository.findById(messageRequest.getRoomId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Room ID 입니다."));

        User user = userRepository.findById(messageRequest.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User ID 입니다."));

        Message message = new Message();
        message.setRoom(room);
        message.setUser(user);
        message.setMessage(messageRequest.getMessage());

        Message savedMessage = messageRepository.save(message);

        return new MessageResponse(
            savedMessage.getRoom().getId(),
            savedMessage.getUser().getId(),
            savedMessage.getMessage(),
            savedMessage.getIsRead(),
            DateTimeFormatter.ISO_INSTANT.format(savedMessage.getCreatedAt())
        );
    }

    @Override
    public List<MessageResponse> getMessagesByRoomId(Integer roomId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Room ID 입니다."));

        return messageRepository.findByRoom(room).stream()
            .map(msg -> new MessageResponse(
                msg.getRoom().getId(),
                msg.getUser().getId(),
                msg.getMessage(),
                msg.getIsRead(),
                DateTimeFormatter.ISO_INSTANT.format(msg.getCreatedAt())
            ))
            .collect(Collectors.toList());
    }
}

