package com.sesac.carematching.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponse {
    private String roomId;
    private String username;
    private String message;
    private String createdAt;
    private String createdDate; // 월/일 (예: "06/18")
    private String createdTime; // 시/분 (예: "14:45")
    private boolean isRead; // 메시지 읽음 여부

    public MessageResponse(
            @NotNull String roomId,
            @NotNull String username,
            @NotNull String message,
            @NotNull String createdAt,
            @NotNull String createdDate,
            @NotNull String createdTime,
            boolean isRead
    ) {
        this.roomId = roomId;
        this.username = username; // ✅ userId 제거, username만 반환
        this.message = message;
        this.createdAt = createdAt;
        this.createdDate = createdDate;
        this.createdTime = createdTime;
        this.isRead = isRead;
    }

    // 기존 호출을 위한 오버로드: 기본적으로 isRead=false
    public MessageResponse(
            @NotNull String roomId,
            @NotNull String username,
            @NotNull String message,
            @NotNull String createdAt,
            @NotNull String createdDate,
            @NotNull String createdTime
    ) {
        this(roomId, username, message, createdAt, createdDate, createdTime, false);
    }
}
