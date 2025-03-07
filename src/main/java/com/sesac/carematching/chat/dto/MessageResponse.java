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
    private String createdDate; // 월/일 (예: "06/18")
    private String createdTime; // 시/분 (예: "14:45")

    public MessageResponse(Integer roomId, Integer userId, String username, String message, boolean isRead, String createdAt,  String createdDate, String createdTime) {
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
    }

    // Getter, Setter 추가
}
