package com.sesac.carematching.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class MessageResponse {
    private Integer roomId;
    private Integer userId;
    private String username; // 👈 추가됨
    private String message;
    private boolean isRead;
    private String createdAt;

    public MessageResponse(Integer roomId, Integer userId, String username, String message, boolean isRead, String createdAt) {
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getter, Setter 추가
}
