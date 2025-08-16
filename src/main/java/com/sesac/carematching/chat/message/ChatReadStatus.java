package com.sesac.carematching.chat.message;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "chat_read_status")
public class ChatReadStatus {
    @Id
    private String id; // optional Mongo id

    private String roomId;
    private String userId;
    private Instant readAt;
}
