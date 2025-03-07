package com.sesac.carematching.community.comment;

import com.sesac.carematching.user.User;
import lombok.Getter;

import java.time.Instant;

@Getter
public class CommentResponse {
    private Integer id;
    private String profileImage;
    private String nickname;
    private String role;
    private String content;
    private Instant createdAt;
    private boolean isAuthor;

    public CommentResponse(Comment comment, User user, boolean isAuthor) {
        this.id = comment.getId();
        this.profileImage = comment.getIsAnonymous() ? null : user.getProfileImage();
        this.nickname = comment.getIsAnonymous() ? "익명" : user.getNickname();

        if(user.getRole().getId() == 1) {
            this.role = "관리자";
        }else if(user.getRole().getId() == 2) {
            this.role = "요양사";
        }else{
            this.role = "수급자";
        }

        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.isAuthor = isAuthor;
    }
}
