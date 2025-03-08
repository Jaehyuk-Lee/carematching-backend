package com.sesac.carematching.token;

import com.sesac.carematching.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {
    @Id
    @Column(name = "USERNAME")
    private String username;

    @Column(name = "TOKEN")
    private String token;

    public RefreshToken(String username, String token) {
        this.username = username;
        this.token = token;
    }
}
