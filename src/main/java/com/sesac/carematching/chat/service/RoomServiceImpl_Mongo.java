package com.sesac.carematching.chat.service;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverRepository;
import com.sesac.carematching.chat.RoomBuildException;
import com.sesac.carematching.chat.dto.MessageResponse;
import com.sesac.carematching.chat.dto.RoomResponse;
import com.sesac.carematching.chat.message.MongoMessage;
import com.sesac.carematching.chat.message.MongoMessageRepository;
import com.sesac.carematching.chat.room.MongoRoom;
import com.sesac.carematching.chat.room.MongoRoomRepository;
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
public class RoomServiceImpl_Mongo implements RoomService<String> {

    private final MongoRoomRepository mongoRoomRepository;
    private final UserRepository userRepository;
    private final CaregiverRepository caregiverRepository;
    private final MongoMessageRepository mongoMessageRepository;
    private final NotificationService notificationService;


    @Transactional
    public RoomResponse<String> createRoom(String requesterUsername, Integer caregiverId) {
        // (1) 요청자 조회
        User requester = userRepository.findByUsername(requesterUsername)
            .orElseThrow(() -> new RoomBuildException("요청자 정보가 존재하지 않습니다."));

        // (2) 요청자가 요양사이면 에러
        if (caregiverRepository.existsByUser(requester)) {
            throw new RoomBuildException("요양사는 요청자가 될 수 없습니다.");
        }

        // (3) caregiverId로 수신자(Caregiver) 조회 → receiver(User)
        Caregiver caregiver = caregiverRepository.findById(caregiverId)
            .orElseThrow(() -> new RoomBuildException("존재하지 않는 요양사입니다."));
        User receiver = caregiver.getUser();

        // (4) 중복된 방이 있는지 확인 (MongoDB)
        boolean roomExists = mongoRoomRepository.existsByRequesterUserIdAndReceiverUserId(requester.getId(), receiver.getId());
        if (roomExists) {
            throw new RoomBuildException("이미 해당 요양사와 채팅방이 존재합니다.");
        }

        // (5) MongoDB에 방 생성
        MongoRoom mongoRoom = new MongoRoom();
        mongoRoom.setRequesterUserId(requester.getId());
        mongoRoom.setRequesterUsername(requester.getUsername());
        mongoRoom.setReceiverUserId(receiver.getId());
        mongoRoom.setReceiverUsername(receiver.getUsername());
        mongoRoom.setCreatedAt(Instant.now());

        MongoRoom savedMongoRoom = mongoRoomRepository.save(mongoRoom);

        // (6) 상대방(요양사)에게 알림 전송
        notificationService.sendNotificationToUser(receiver.getUsername(), "새로운 매칭 신청이 왔습니다!");

        // (7) 방 응답 DTO
        return new RoomResponse(
            savedMongoRoom.getId(), // MongoDB의 id 사용
            savedMongoRoom.getRequesterUsername(),
            savedMongoRoom.getReceiverUsername(),
            savedMongoRoom.getCreatedAt(),
            "",
            null,
            "메시지가 없습니다.",
            "01/01",
            null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse<String> getRoom(String roomId) {
        // 1) MongoRoom 문서 조회
        MongoRoom room = mongoRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Room ID 입니다."));

        // 2) Room에 연결된 메시지 목록 조회 (MongoDB에서)
        List<MessageResponse<String>> messages = mongoMessageRepository.findByRoomId(roomId).stream()
            .map(message -> {
                String formattedDate = message.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MM/dd"));
                String formattedTime = message.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
                return new MessageResponse<>(
                    message.getRoomId(),
                    message.getUsername(),
                    message.getMessage(),
                    message.getCreatedAt().toString(),
                    formattedDate,
                    formattedTime
                );
            })
            .collect(Collectors.toList());

        // 3) 마지막 메시지 가져오기 (MongoDB에서)
        Optional<MongoMessage> lastMessageOpt = mongoMessageRepository.findTopByRoomIdOrderByCreatedAtDesc(roomId);
        String lastMessageText = lastMessageOpt.map(MongoMessage::getMessage).orElse("메시지가 없습니다.");

        // 👇 마지막 메시지 날짜 (월/일 형식)
        String lastMessageDate = lastMessageOpt.map(message ->
            message.getCreatedAt()
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MM/dd"))
        ).orElse("");

        // 4) RoomResponse로 변환하여 메시지 목록 포함
        return new RoomResponse<String>(
            room.getId(),
            room.getRequesterUsername(),
            room.getReceiverUsername(),
            room.getCreatedAt(),
            "", // 상대방 username (개별 조회 시 필요 없음)
            messages,
            lastMessageText,
            lastMessageDate,
            null
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<RoomResponse<String>> getUserRooms(String username) {
        // 1. 현재 사용자의 정보를 조회합니다.
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // 2. 사용자가 참여 중인 채팅방들을 조회합니다. (MongoDB에서)
        List<MongoRoom> rooms = mongoRoomRepository.findByRequesterUserIdOrReceiverUserId(user.getId(), user.getId());

        // 3. 각 Room에 대해 상대방 정보를 정확하게 판단합니다.
        return rooms.stream().map(room -> {
            Integer requesterUserId = room.getRequesterUserId();
            Integer receiverUserId = room.getReceiverUserId();
            String requesterUsername = room.getRequesterUsername();
            String receiverUsername = room.getReceiverUsername();

            // 현재 사용자의 상대방을 판단합니다.
            Integer otherUserId;

            if (requesterUserId.equals(user.getId())) {
                otherUserId = receiverUserId;
            } else {
                otherUserId = requesterUserId;
            }

            // 4. 상대방 사용자 정보 조회
            User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("상대방 사용자 정보를 찾을 수 없습니다."));

            // 5. 상대방이 Caregiver인지 여부를 확인하고, 그에 맞게 정보를 가져옵니다.
            boolean isCaregiver = caregiverRepository.existsByUser(otherUser);

            String displayName;
            String profileImage;

            if (isCaregiver) {
                Caregiver cg = caregiverRepository.findByUser(otherUser)
                    .orElseThrow(() -> new IllegalStateException("Caregiver 엔티티를 찾을 수 없습니다."));
                displayName = cg.getRealName();
                profileImage = cg.getCaregiverImage();  // caregiver 전용 이미지
            } else {
                displayName = otherUser.getNickname();
                profileImage = otherUser.getProfileImage();  // 일반 User 이미지
            }

            // 6. 마지막 메시지 정보도 함께 조회합니다. (MongoDB에서)
            Optional<MongoMessage> lastMessageOpt = mongoMessageRepository.findTopByRoomIdOrderByCreatedAtDesc(room.getId());
            String lastMessageText = lastMessageOpt.map(MongoMessage::getMessage).orElse("메시지가 없습니다.");
            String lastMessageDate = lastMessageOpt.map(message ->
                message.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MM/dd"))
            ).orElse("");

            return new RoomResponse<>(
                room.getId(),
                requesterUsername,
                receiverUsername,
                room.getCreatedAt(),
                displayName,
                List.of(),
                lastMessageText,
                lastMessageDate,
                profileImage
            );
        }).collect(Collectors.toList());
    }

}
