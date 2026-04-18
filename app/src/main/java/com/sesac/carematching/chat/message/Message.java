package com.sesac.carematching.chat.message;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class Message {

    @Id
    @NotNull
    @Field(targetType = FieldType.OBJECT_ID)
    private String id;

    @NotNull
    @Field(targetType = FieldType.OBJECT_ID)
    private String roomId;

    @NotNull
    private Integer userId;

    @NotNull
    private String username;

    @Size(max = 500)
    @NotNull
    private String message;

    @NotNull
    private Instant createdAt;

    public Message() {
        this.createdAt = Instant.now();
    }
}
