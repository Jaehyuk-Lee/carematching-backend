package com.sesac.carematching.user.dto;

import lombok.Data;

@Data
public class UserCertListDTO {
    private String username;
    private String nickname;
    private String certno;
    private boolean pending;

    public UserCertListDTO(String username, String nickname, String certno, boolean pending) {
        this.username = username;
        this.nickname = nickname;
        this.certno = certno;
        this.pending = pending;
    }
}
