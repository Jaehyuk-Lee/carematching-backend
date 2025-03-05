package com.sesac.carematching.community.post;

import com.sesac.carematching.config.EnvProperties;
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

        String bucketName = EnvProperties.getS3BucketName();
        this.profileImage = (post.getIsAnonymous() || user.getProfileImage() == null || user.getProfileImage().isEmpty()) ? "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/user_profile_image/basicprofileimage.png" : user.getProfileImage();

        this.nickname = post.getIsAnonymous() ? "익명" : user.getNickname();
        this.relativeTime = relativeTime;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
}
