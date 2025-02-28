package com.sesac.carematching.chat.message;

import com.sesac.carematching.chat.room.Room;
import com.sesac.carematching.chat.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByRoom(Room room);
    List<Message> findByRoomId(Integer roomId);
}
