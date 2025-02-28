package com.sesac.carematching.community.post;

import com.sesac.carematching.user.User;
import lombok.Getter;

@Getter
public class CommunityPostListResponse {
    private Integer id;
    private String title;
    private String content;
    private String image;
    private String profileImage;
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
        this.profileImage = "사용자 프로필 이미지 url";  // 실제로는 User 엔티티에 profileImage 필드를 추가한 뒤 활용
        this.nickname = user.getNickname();
        this.relativeTime = relativeTime;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
}
