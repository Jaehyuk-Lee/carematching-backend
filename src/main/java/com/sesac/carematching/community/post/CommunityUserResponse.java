package com.sesac.carematching.community.post;

import com.sesac.carematching.config.EnvProperties;
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

        String bucketName = EnvProperties.getS3BucketName();
        this.profileImage = (user.getProfileImage() == null || user.getProfileImage().isEmpty()) ? "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/user_profile_image/basicprofileimage.png" : user.getProfileImage();

        this.nickname = user.getNickname();
        this.postCount = postCount;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
    }
}
