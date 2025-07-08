package com.sesac.carematching.chat.message;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByRoomId(String roomId);
    Optional<Message> findTopByRoomIdOrderByCreatedAtDesc(String roomId);
}
