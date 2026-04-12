package com.sesac.carematching.community.post;

import lombok.Getter;

@Getter
public class CommunityPostUpdateResponse {
    private String category;      // "ALL" or "CAREGIVER"
    private Boolean isAnonymous;
    private String title;
    private String content;
    private String image;         // 기존에 업로드된 이미지 URL

    public CommunityPostUpdateResponse(String category, Boolean isAnonymous, String title, String content, String image) {
        this.category = category;
        this.isAnonymous = isAnonymous;
        this.title = title;
        this.content = content;
        this.image = image;
    }
}
