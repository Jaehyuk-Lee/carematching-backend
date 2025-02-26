package com.sesac.carematching.community.post;

import com.sesac.carematching.user.User;
import lombok.Getter;

@Getter
public class CommunityPostListResponse {
    private Integer id;
    private String title;
    private String content;
    private String image;
    private String nickname;
    private String relativeTime;    // "몇 시간 전", "몇 일 전" 등
    private int viewCount;
    private int likeCount;
    private int commentCount;

    public CommunityPostListResponse(Post post, User user, String relativeTime, int viewCount, int likeCount, int commentCount) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.image = post.getImage();
        this.nickname = user.getNickname();
        this.relativeTime = relativeTime;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
}
