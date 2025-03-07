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
        this.profileImage = user.getProfileImage();
        this.nickname = user.getNickname();
        this.postCount = postCount;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
    }
}
