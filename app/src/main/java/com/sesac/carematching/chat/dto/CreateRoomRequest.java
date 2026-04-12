package com.sesac.carematching.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoomRequest {

    private Integer caregiverId; // 요양사 ID (CNO)


    @Override
    public String toString() {
        return "CreateRoomRequest{" +
            "caregiverId=" + caregiverId +
            '}';
    }
}

