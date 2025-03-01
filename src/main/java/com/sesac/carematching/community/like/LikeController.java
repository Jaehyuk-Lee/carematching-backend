package com.sesac.carematching.community.like;

import com.sesac.carematching.community.post.CommunityPostListResponse;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/community")
public class LikeController {

    private final LikeService likeService;
    private final UserService userService;
    private final TokenAuth tokenAuth;

    /**
     * "내가 좋아요한 게시글" 조회 (좋아요 누른 시간 desc)
     */
    @GetMapping("/my-likes")
    public ResponseEntity<Page<CommunityPostListResponse>> getMyLikedPosts(
        HttpServletRequest request,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        String username = tokenAuth.extractUsernameFromToken(request);

        User user = userService.getUserInfo(username);

        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityPostListResponse> result = likeService.getMyLikedPosts(user, pageable);

        return ResponseEntity.ok(result);
    }
}
