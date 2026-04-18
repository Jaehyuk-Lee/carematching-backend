package com.sesac.carematching.community.comment;

import com.sesac.carematching.community.post.Post;
import lombok.Getter;

@Getter
public class MyCommentListResponse {
    private Integer postId;
    private String content;
    private String relativeTime;    // "몇 시간 전", "몇 일 전" 등

    public MyCommentListResponse(Post post, Comment comment, String relativeTime) {
        this.postId = post.getId();
        this.content = comment.getContent();
        this.relativeTime = relativeTime;
    }
}
