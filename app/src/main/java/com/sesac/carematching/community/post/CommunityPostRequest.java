package com.sesac.carematching.community.post;

import lombok.Getter;

@Getter
public class CommunityPostRequest {
    private String category;
    private Boolean isAnonymous;
    private String title;
    private String content;

    public CommunityPostRequest(String category, Boolean isAnonymous, String title, String content) {
        this.category = category;
        this.isAnonymous = isAnonymous;
        this.title = title;
        this.content = content;
    }
}
