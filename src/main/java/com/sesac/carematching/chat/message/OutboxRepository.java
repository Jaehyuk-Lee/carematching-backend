package com.sesac.carematching.chat.message;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends MongoRepository<OutboxMessage, String> {
    List<OutboxMessage> findByStatus(String status);
}
