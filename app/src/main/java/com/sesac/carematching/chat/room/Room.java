package com.sesac.carematching.chat.room;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "chat_rooms")
@CompoundIndex(def = "{'requesterUserId': 1, 'receiverUserId': 1}", unique = true)
public class Room {

    @Id
    @NotNull
    @Field(targetType = FieldType.OBJECT_ID)
    private String id;

    @NotNull
    private Integer requesterUserId;

    @NotNull
    private String requesterUsername;

    @NotNull
    private Integer receiverUserId;

    @NotNull
    private String receiverUsername;

    @NotNull
    private Instant createdAt;

    public Room() {
        this.createdAt = Instant.now();
    }
}
