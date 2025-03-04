package com.sesac.carematching.community.like;

import com.sesac.carematching.community.post.CommunityPostListResponse;
import com.sesac.carematching.community.post.Post;
import com.sesac.carematching.community.post.PostRepository;
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
    private final PostRepository postRepository; // 주입하여 게시글 조회

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

    /**
     * 좋아요 상태 업데이트 API
     * 프론트에서 { "postId": ..., "isLiked": true/false } 형태로 전송
     */
    @PostMapping("/like")
    public ResponseEntity<String> updateLikeStatus(
            HttpServletRequest request,
            @RequestBody LikeRequest likeRequest
    ) {

        System.out.println("postId = " + likeRequest.getPostId());
        System.out.println("isLiked = " + likeRequest.isLiked());

        // 1) 토큰에서 사용자 정보 추출
        String username = tokenAuth.extractUsernameFromToken(request);
        User user = userService.getUserInfo(username);

        // 2) 대상 게시글 조회
        Post post = postRepository.findById(likeRequest.getPostId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

        // 3) 좋아요 상태 업데이트
        likeService.updateLikeStatus(user, post, likeRequest.isLiked());

        return ResponseEntity.ok("좋아요 상태가 업데이트되었습니다.");
    }
}
