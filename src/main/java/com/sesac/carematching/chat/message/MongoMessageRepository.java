package com.sesac.carematching.chat.message;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MongoMessageRepository extends MongoRepository<MongoMessage, String> {
    List<MongoMessage> findByRoomId(String roomId);
    Optional<MongoMessage> findTopByRoomIdOrderByCreatedAtDesc(String roomId);
}
