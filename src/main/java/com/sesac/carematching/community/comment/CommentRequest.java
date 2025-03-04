package com.sesac.carematching.community.comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    private Integer postId;
    private String content;
    private boolean isAnonymous;
}
