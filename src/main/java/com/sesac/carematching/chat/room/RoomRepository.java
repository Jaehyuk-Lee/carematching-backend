package com.sesac.carematching.chat.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    // User가 요청자이거나 수신자인 채팅방 조회 (receiverUserId = Caregiver의 UNO)
    List<Room> findByRequesterIdOrReceiverId(Integer requesterUserId, Integer receiverUserId);
}


