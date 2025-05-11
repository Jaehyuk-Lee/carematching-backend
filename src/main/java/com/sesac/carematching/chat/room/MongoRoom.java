package com.sesac.carematching.chat.room;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "chat_rooms")
public class MongoRoom {

    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    private String id;

    private Integer requesterUserId;

    private String requesterUsername;

    private Integer receiverUserId;

    private String receiverUsername;

    private Instant createdAt;

    public MongoRoom() {
        this.createdAt = Instant.now();
    }
}
