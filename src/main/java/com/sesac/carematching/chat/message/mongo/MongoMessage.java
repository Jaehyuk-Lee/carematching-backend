package com.sesac.carematching.chat.message.mongo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "chat_messages")
public class MongoMessage {

    @Id
    private String id;

    private Integer roomId;

    private Integer userId;

    private String username;

    private String message;

    private Boolean isRead;

    private Instant createdAt;

    public MongoMessage() {
        this.isRead = false;
        this.createdAt = Instant.now();
    }
}
