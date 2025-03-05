package com.sesac.carematching.community.post;

import com.sesac.carematching.user.User;
import lombok.Getter;

import java.time.Instant;

@Getter
public class CommunityPostDetailResponse {
    private Integer postId;
    private String profileImage;
    private String nickname;
    private String role;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private String category;
    private String title;
    private String content;
    private String image;
    private boolean isLiked;    // 현재 로그인한 사용자가 게시물을 좋아하는지
    private Instant createdAt;
    private boolean isAuthor;   // 현재 로그인한 사용자가 게시물의 작성자인지

    public CommunityPostDetailResponse(Post post, User user, int viewCount, int likeCount, int commentCount, boolean isLiked, boolean isAuthor) {
        this.postId = post.getId();
        this.profileImage = (post.getIsAnonymous() || user.getProfileImage() == null || user.getProfileImage().isEmpty()) ? "https://test-carematching-uploaded-files.s3.ap-northeast-2.amazonaws.com/user_profile_image/basicprofileimage.png" : user.getProfileImage();
        this.nickname = post.getIsAnonymous() ? "익명" : user.getNickname();
        if(user.getRole().getId() == 1) {
            this.role = "관리자";
        }else if(user.getRole().getId() == 2) {
            this.role = "요양사";
        }else{
            this.role = "수급자";
        }
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.category = post.getCategory().getName();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.image = post.getImage();
        this.isLiked = isLiked;
        this.createdAt = post.getCreatedAt();
        this.isAuthor = isAuthor;
    }
}
