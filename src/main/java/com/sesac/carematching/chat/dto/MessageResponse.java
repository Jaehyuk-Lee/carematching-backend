package com.sesac.carematching.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MessageResponse {
    private Integer roomId;
    private Integer userId;
    private String message;
    private Boolean isRead;
    private String createdAt;
}
