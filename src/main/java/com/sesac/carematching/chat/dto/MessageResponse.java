package com.sesac.carematching.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class MessageResponse {
    private Integer roomId;
    private String username;
    private String message;
    private boolean isRead;
    private String createdAt;
    private String createdDate; // 월/일 (예: "06/18")
    private String createdTime; // 시/분 (예: "14:45")

    public MessageResponse(
            @NotNull Integer roomId,
            @NotNull String username,
            @NotNull String message,
            boolean isRead,
            @NotNull String createdAt,
            @NotNull String createdDate,
            @NotNull String createdTime
    ) {
        this.roomId = roomId;
        this.username = username; // ✅ userId 제거, username만 반환
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
    }
}
