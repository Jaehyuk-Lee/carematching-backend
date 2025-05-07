package com.sesac.carematching.chat.message.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoMessageRepository extends MongoRepository<MongoMessage, String> {
    List<MongoMessage> findByRoomId(Integer roomId);
}
