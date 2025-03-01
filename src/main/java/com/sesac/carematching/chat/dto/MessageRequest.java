package com.sesac.carematching.chat.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    private Integer roomId;   // 채팅방 ID
    private Integer userId;   // 보낸 사람 (User ID)
    private String username;  // 보낸 사람 (Username) - 추가
    private String message;   // 메시지 내용

    @Override
    public String toString() {
        return "MessageRequest{" +
            "roomId=" + roomId +
            ", userId=" + userId +
            ", username='" + username + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}

