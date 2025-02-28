package com.sesac.carematching.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoomRequest {
    private Integer requesterUserId;
    private Integer caregiverId;

    @Override
    public String toString() {
        return "CreateRoomRequest{" +
            "requesterUserId=" + requesterUserId +
            ", caregiverId=" + caregiverId +
            '}';
    }
}

