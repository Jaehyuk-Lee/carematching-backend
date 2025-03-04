package com.sesac.carematching.community.like;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeRequest {
    private Integer postId;
    private boolean isLiked; // true: 현재 좋아요 상태 → 삭제, false: 현재 좋아요가 아님 → 추가
}
