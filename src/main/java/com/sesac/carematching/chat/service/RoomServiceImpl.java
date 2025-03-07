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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final CaregiverRepository caregiverRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest createRoomRequest) {
        System.out.println("🔍 [DEBUG] 서비스 계층 받은 요청 데이터: " + createRoomRequest);

        // 1️⃣ 요청자(User) 조회
        User requester = userRepository.findById(createRoomRequest.getRequesterUserId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 User ID 입니다."));

        // 2️⃣ Caregiver 조회 (caregiverId를 기반으로)
        Caregiver caregiver = caregiverRepository.findById(createRoomRequest.getCaregiverId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Caregiver ID 입니다."));

        // 3️⃣ Caregiver의 User ID(UNO) 조회
        User receiver = caregiver.getUser(); // Caregiver의 User 정보 가져오기
        System.out.println("🔍 [DEBUG] Caregiver의 User ID(UNO)로 receiverUserId 설정: " + receiver.getId());

        // 4️⃣ Room 엔티티 생성 및 설정
        Room room = new Room();
        room.setRequester(requester); // 요청자(User) 설정
        room.setReceiver(receiver); // **Caregiver의 User ID 저장**

        // 5️⃣ Room 엔티티 저장
        Room savedRoom = roomRepository.save(room);
        System.out.println("💾 [INFO] 채팅방이 저장되었습니다. Room ID: " + savedRoom.getId());

        // 6️⃣ RoomResponse 반환
        return new RoomResponse(
            savedRoom.getId(),
            savedRoom.getRequester().getId(),
            savedRoom.getReceiver().getId(), // **Caregiver의 UNO(User ID)가 들어감**
            savedRoom.getCreatedAt(),
            "", // 상대방 username (새로운 채팅방이므로 빈 값)
            List.of(), // 새로 생성된 채팅방은 메시지가 없으므로 빈 리스트 전달
            "메시지가 없습니다.", // 마지막 메시지도 없음
            "1월 1일"
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

        // 3) 마지막 메시지 가져오기
        Optional<Message> lastMessageOpt = messageRepository.findTopByRoomIdOrderByCreatedAtDesc(roomId);
        String lastMessageText = lastMessageOpt.map(Message::getMessage).orElse("메시지가 없습니다.");

        // 👇 마지막 메시지 날짜 (월/일 형식)
        String lastMessageDate = lastMessageOpt.map(message ->
            message.getCreatedAt()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MM/dd"))
        ).orElse("");
        // 4) RoomResponse로 변환하여 메시지 목록 포함
        return new RoomResponse(
            room.getId(),
            room.getRequester().getId(),
            room.getReceiver().getId(),
            room.getCreatedAt(),
            "", // 상대방 username (개별 조회 시 필요 없음)
            messages, // 메시지 목록 포함
            lastMessageText,
            lastMessageDate
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getUserRooms(Integer userId) {
        // 1. User가 참여 중인 채팅방을 모두 조회
        List<Room> rooms = roomRepository.findByRequesterIdOrReceiverId(userId, userId);

        // 2. Room을 RoomResponse로 변환하여 반환
        return rooms.stream().map(room -> {
            // 👇 현재 로그인한 사용자가 아닌 상대방 userId 가져오기
            Integer otherUserId = room.getRequester().getId().equals(userId) ? room.getReceiver().getId() : room.getRequester().getId();

            // 👇 상대방 username 가져오기
            String otherUsername = userRepository.findById(otherUserId)
                .map(User::getNickname)
                .orElse("알 수 없음");

            // 👇 마지막 메시지 가져오기
            Optional<Message> lastMessageOpt = messageRepository.findTopByRoomIdOrderByCreatedAtDesc(room.getId());
            String lastMessageText = lastMessageOpt.map(Message::getMessage).orElse("메시지가 없습니다.");
            // 👇 마지막 메시지 날짜 (월/일 형식)
            String lastMessageDate = lastMessageOpt.map(message ->
                message.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MM/dd"))
            ).orElse("");
            return new RoomResponse(
                room.getId(),
                room.getRequester().getId(),
                room.getReceiver().getId(),
                room.getCreatedAt(),
                otherUsername, // 상대방 username 추가
                List.of(), // 메시지는 빈 리스트로 전달
                lastMessageText, // 마지막 메시지 추가
                lastMessageDate
            );
        }).collect(Collectors.toList());
    }

    /**
     * Message 엔티티를 MessageResponse DTO로 변환
     */
    private MessageResponse convertToMessageResponse(Message message) {
        // 각 메시지의 생성시간을 "MM/dd" (날짜)와 "HH:mm" (시간)으로 포맷팅
        String formattedDate = message.getCreatedAt()
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MM/dd"));
        String formattedTime = message.getCreatedAt()
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"));

        return new MessageResponse(
            message.getRoom().getId(),
            message.getUser().getId(),
            message.getUser().getUsername(),
            message.getMessage(),
            message.getIsRead(),
            message.getCreatedAt().toString(),
            formattedDate,
            formattedTime
        );
    }
}
