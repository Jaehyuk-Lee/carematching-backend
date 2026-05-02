package com.sesac.carematching.community.post;

import com.sesac.carematching.community.category.Category;
import lombok.Getter;

@Getter
public class MyPostListResponse {
    private Integer id;
    private String title;
    private String content;
    private String image;
    private String category;
    private String relativeTime;    // "몇 시간 전", "몇 일 전" 등
    private int viewCount;
    private int likeCount;
    private int commentCount;

    public MyPostListResponse(Post post, Category category, String relativeTime, int viewCount, int likeCount, int commentCount) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.image = post.getImage();
        this.category = category.getName();
        this.relativeTime = relativeTime;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
}
