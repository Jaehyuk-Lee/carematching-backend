package com.sesac.carematching.chat.service;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverRepository;
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

import java.time.Instant;
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


    @Transactional
    public RoomResponse createRoom(String requesterUsername, Integer caregiverId) {
        // (1) 요청자 조회
        User requester = userRepository.findByUsername(requesterUsername)
            .orElseThrow(() -> new IllegalArgumentException("요청자 정보가 존재하지 않습니다."));

        // (2) 요청자가 요양사이면 에러
        if (caregiverRepository.existsByUser(requester)) {
            throw new SecurityException("요양사는 요청자가 될 수 없습니다.");
        }

        // (3) caregiverId로 수신자(Caregiver) 조회 → receiver(User)
        Caregiver caregiver = caregiverRepository.findById(caregiverId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요양사입니다."));
        User receiver = caregiver.getUser();

        // (4) 중복된 방이 있는지 확인
        boolean roomExists = roomRepository.existsByRequesterAndReceiver(requester, receiver);
        if (roomExists) {
            throw new IllegalStateException("이미 해당 요양사와 채팅방이 존재합니다.");
        }

        // (5) 방 생성
        Room room = new Room();
        room.setRequester(requester);
        room.setReceiver(receiver);
        room.setCreatedAt(Instant.now());

        Room savedRoom = roomRepository.save(room);

        // (6) 방 응답 DTO
        return new RoomResponse(
            savedRoom.getId(),
            savedRoom.getRequester().getUsername(),
            savedRoom.getReceiver().getUsername(),
            savedRoom.getCreatedAt(),
            "",
            null,
            "메시지가 없습니다.",
            "01/01"
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
            room.getRequester().getUsername(),
            room.getReceiver().getUsername(),
            room.getCreatedAt(),
            "", // 상대방 username (개별 조회 시 필요 없음)
            messages,
            lastMessageText,
            lastMessageDate
        );
    }

    @Override
    public List<RoomResponse> getUserRooms(Integer id) {
        return List.of();
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoomResponse> getUserRooms(String username) {
        // 1. User가 참여 중인 채팅방을 모두 조회
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        List<Room> rooms = roomRepository.findByRequesterOrReceiver(user, user);

        // 2. Room을 RoomResponse로 변환하여 반환
        return rooms.stream().map(room -> {
            // 상대방 user 객체
            User otherUser = room.getRequester().equals(user) ? room.getReceiver() : room.getRequester();

            // ⭐ 요양사 여부 판별: caregiverRepository.existsByUser(otherUser)
            boolean isCaregiver = caregiverRepository.existsByUser(otherUser);

            // 실제로 Caregiver 엔티티를 가져와서 realName을 꺼내고 싶다면:
            String displayName;
            if (isCaregiver) {
                Caregiver cg = caregiverRepository.findByUser(otherUser)
                    .orElseThrow(() -> new IllegalStateException("Caregiver 엔티티를 찾을 수 없습니다."));
                displayName = cg.getRealName();
            } else {
                displayName = otherUser.getNickname(); // 일반 유저의 닉네임
            }

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
                room.getRequester().getUsername(),
                room.getReceiver().getUsername(),
                room.getCreatedAt(),
                displayName, // 상대방 username 추가
                List.of(),
                lastMessageText,
                lastMessageDate
            );
        }).collect(Collectors.toList());
    }

    /**
     * Message 엔티티를 MessageResponse DTO로 변환
     */
    private MessageResponse convertToMessageResponse(Message message) {
        // 1) 메시지 생성시간 포맷팅
        String formattedDate = message.getCreatedAt()
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MM/dd"));
        String formattedTime = message.getCreatedAt()
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"));

        // 2) MessageResponse로 변환 (null 방지)
        return new MessageResponse(
            message.getRoom() != null ? message.getRoom().getId() : null,
            message.getUser() != null ? message.getUser().getUsername() : "알 수 없음",
            message.getMessage() != null ? message.getMessage() : "내용 없음",
            message.getIsRead(),
            message.getCreatedAt() != null ? message.getCreatedAt().toString() : "알 수 없음",
            formattedDate,
            formattedTime
        );
    }
}
