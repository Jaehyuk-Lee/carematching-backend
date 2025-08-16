package com.sesac.carematching.chat.message;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatReadStatusRepository extends MongoRepository<ChatReadStatus, String> {
    Optional<ChatReadStatus> findByRoomIdAndUserId(String roomId, String userId);
}
