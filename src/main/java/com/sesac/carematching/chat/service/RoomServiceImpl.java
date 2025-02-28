package com.sesac.carematching.chat.service;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverRepository;
import com.sesac.carematching.chat.dto.CreateRoomRequest;
import com.sesac.carematching.chat.dto.MessageResponse;
import com.sesac.carematching.chat.dto.RoomResponse;
import com.sesac.carematching.chat.message.Message;
import com.sesac.carematching.chat.message.MessageRepository;
import com.sesac.carematching.chat.room.Room;
import com.sesac.carematching.chat.room.RoomRepository;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final CaregiverRepository caregiverRepository;
    private final MessageRepository messageRepository; // 추가: MessageRepository 주입

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest createRoomRequest) {
        System.out.println("🔍 [DEBUG] 서비스 계층 받은 요청 데이터: " + createRoomRequest);

        // 1) User 및 Caregiver 조회
        User requester = userRepository.findById(createRoomRequest.getRequesterUserId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User ID 입니다."));
        Caregiver caregiver = caregiverRepository.findById(createRoomRequest.getCaregiverId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Caregiver ID 입니다."));

        // 2) Room 엔티티 생성 및 설정
        Room room = new Room();
        room.setRequester(requester);
        room.setCaregiver(caregiver);

        // 3) Room 엔티티 저장
        Room savedRoom = roomRepository.save(room);
        System.out.println("💾 [INFO] 채팅방이 저장되었습니다. Room ID: " + savedRoom.getId());

        // 4) 빈 메시지 목록과 함께 RoomResponse 반환
        return new RoomResponse(
            savedRoom.getId(),
            savedRoom.getRequester().getId(),
            savedRoom.getCaregiver().getId(),
            savedRoom.getCreatedAt(),
            List.of() // 새로 생성된 채팅방은 메시지가 없으므로 빈 리스트 전달
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoom(Integer roomId) {
        // 1) Room 엔티티 조회
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Room ID 입니다."));

        // 2) Room에 연결된 메시지 목록 조회
        List<MessageResponse> messages = messageRepository.findByRoomId(roomId).stream()
            .map(this::convertToMessageResponse)
            .collect(Collectors.toList());

        // 3) RoomResponse로 변환하여 메시지 목록 포함
        return new RoomResponse(
            room.getId(),
            room.getRequester().getId(),
            room.getCaregiver().getId(),
            room.getCreatedAt(),
            messages // 메시지 목록 포함
        );
    }

    /**
     * Message 엔티티를 MessageResponse DTO로 변환
     */
    private MessageResponse convertToMessageResponse(Message message) {
        return new MessageResponse(
            message.getRoom().getId(),
            message.getUser().getId(),
            message.getMessage(),
            message.getIsRead(),
            message.getCreatedAt().toString()
        );
    }
}
