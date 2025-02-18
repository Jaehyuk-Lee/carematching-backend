package com.sesac.carematching.chatRoom.message;

import com.sesac.carematching.chatRoom.ChatRoom;
import com.sesac.carematching.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "CHAT_MESSAGE")
public class Message {
    @Id
    @Column(name = "CMNO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "CRNO", nullable = false)
    private ChatRoom crno;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "UNO", nullable = false)
    private User uno;

    @Size(max = 500)
    @NotNull
    @Column(name = "MESSAGE", nullable = false, length = 500)
    private String message;

    @NotNull
    @Column(name = "READ_OR_NOT", nullable = false)
    private Boolean readOrNot = false;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

}
