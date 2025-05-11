package com.sesac.carematching.chat.message;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "chat_messages")
public class MongoMessage {

    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    private String id;

    private String roomId;

    private Integer userId;

    private String username;

    private String message;

    private Instant createdAt;

    public MongoMessage() {
        this.createdAt = Instant.now();
    }
}
