package com.sesac.carematching.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class RoomResponse {
    private Integer roomId;
    private Integer requesterUserId;
    private Integer caregiverId;
    private String createdAt;

    public RoomResponse(
        @NotNull Integer roomId,
        @NotNull Integer requesterUserId,
        @NotNull Integer caregiverId,
        @NotNull Instant createdAt
    ) {
        this.roomId = roomId;
        this.requesterUserId = requesterUserId;
        this.caregiverId = caregiverId;
        this.createdAt = DateTimeFormatter.ISO_INSTANT.format(createdAt);
    }
}
