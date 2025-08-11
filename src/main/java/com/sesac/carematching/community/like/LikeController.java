package com.sesac.carematching.community.like;

import com.sesac.carematching.community.post.CommunityPostListResponse;
import com.sesac.carematching.community.post.Post;
import com.sesac.carematching.community.post.PostRepository;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import com.sesac.carematching.util.TokenAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Tag(name = "Like Controller", description = "커뮤니티 좋아요 관리")
@RestController
@RequestMapping("/api/community")
public class LikeController {

    private final LikeService likeService;
    private final UserService userService;
    private final TokenAuth tokenAuth;
    private final PostRepository postRepository; // 주입하여 게시글 조회

    @Operation(summary = "내가 좋아요한 게시글 목록 조회", description = "로그인한 사용자가 좋아요한 게시글을 페이징 조회합니다.")
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

    @Operation(summary = "게시글 좋아요 상태 변경", description = "게시글의 좋아요 상태를 변경합니다.")
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
