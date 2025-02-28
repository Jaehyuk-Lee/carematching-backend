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
    private List<MessageResponse> messages; // Room과 연결된 메시지 목록

    /**
     * 메시지 목록을 포함한 RoomResponse 생성자
     */
    public RoomResponse(
        @NotNull Integer roomId,
        @NotNull Integer requesterUserId,
        @NotNull Integer caregiverId,
        @NotNull Instant createdAt,
        @NotNull List<MessageResponse> messages
    ) {
        this.roomId = roomId;
        this.requesterUserId = requesterUserId;
        this.caregiverId = caregiverId;
        this.createdAt = DateTimeFormatter.ISO_INSTANT.format(createdAt);
        this.messages = messages; // 메시지 목록 추가
    }
}
