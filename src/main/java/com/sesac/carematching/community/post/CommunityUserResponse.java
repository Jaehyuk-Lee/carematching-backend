package com.sesac.carematching.community.post;

import com.sesac.carematching.user.User;
import lombok.Getter;

@Getter
public class CommunityUserResponse {
    private Integer id;
    private String profileImage;
    private String nickname;
    private int postCount;
    private int commentCount;
    private int likeCount;

    public CommunityUserResponse(User user, int postCount, int commentCount, int likeCount) {
        this.id = user.getId();
        this.profileImage = "사용자 프로필 이미지 url";  // 실제로는 User 엔티티에 profileImage 필드를 추가한 뒤 활용
        this.nickname = user.getNickname();
        this.postCount = postCount;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
    }
}
