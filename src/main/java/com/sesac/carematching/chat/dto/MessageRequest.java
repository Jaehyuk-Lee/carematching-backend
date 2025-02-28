package com.sesac.carematching.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageRequest {
    private Integer roomId; // CRNO
    private Integer userId; // UNO
    private String message;
}
