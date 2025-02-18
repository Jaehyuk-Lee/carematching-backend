package com.sesac.carematching.chatRoom;

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
@Table(name = "CHAT_ROOM")
public class ChatRoom {
    @Id
    @Column(name = "CRNO", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "CTNO", nullable = false)
    private Integer ctno;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "UNO", nullable = false)
    private User uno;

    @Size(max = 500)
    @NotNull
    @Column(name = "LAST_MESSAGE", nullable = false, length = 500)
    private String lastMessage;

    @NotNull
    @Column(name = "MESSAGE_COUNT", nullable = false)
    private Integer messageCount;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @Column(name = "MATCH_STATUS")
    private Boolean matchStatus;

}
