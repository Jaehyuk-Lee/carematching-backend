package com.sesac.carematching.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RoomResponse {
    private Integer roomId;
    private Integer requesterUserId;
    private Integer caregiverId;
    private String createdAt;
    private String otherUsername;
    private List<MessageResponse> messages;
    private String lastMessage;
    private String lastMessageDate; // 👈 마지막 메시지 날짜 (월/일)

    public RoomResponse(
        @NotNull Integer roomId,
        @NotNull Integer requesterUserId,
        @NotNull Integer caregiverId,
        @NotNull Instant createdAt,
        @NotNull String otherUsername,
        @NotNull List<MessageResponse> messages,
        @NotNull String lastMessage,
        @NotNull String lastMessageDate // 👈 추가
    ) {
        this.roomId = roomId;
        this.requesterUserId = requesterUserId;
        this.caregiverId = caregiverId;
        this.otherUsername = otherUsername;
        this.createdAt = DateTimeFormatter.ISO_INSTANT.format(createdAt);
        this.messages = messages;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate; // 👈 추가
    }
}
