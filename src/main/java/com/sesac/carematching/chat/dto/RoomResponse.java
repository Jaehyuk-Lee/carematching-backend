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
public class RoomResponse <T> {
    private T roomId;
    private String requesterUsername;  // UserId → Username
    private String receiverUsername;   // UserId → Username
    private String createdAt;
    private String otherUsername;
    private List<MessageResponse<T>> messages;
    private String lastMessage;
    private String lastMessageDate;
    private String avatar;

    public RoomResponse(
        @NotNull T roomId,
        @NotNull String requesterUsername,  // 👈 변경된 부분
        @NotNull String receiverUsername,   // 👈 변경된 부분
        @NotNull Instant createdAt,
        @NotNull String otherUsername,
        @NotNull List<MessageResponse<T>> messages,
        @NotNull String lastMessage,
        @NotNull String lastMessageDate,
        String avatar
    ) {
        this.roomId = roomId;

        this.requesterUsername = requesterUsername;  // 👈 변경된 부분
        this.receiverUsername = receiverUsername;    // 👈 변경된 부분
        this.otherUsername = otherUsername;
        this.createdAt = DateTimeFormatter.ISO_INSTANT.format(createdAt);
        this.messages = messages;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
        this.avatar = avatar;
    }
}
