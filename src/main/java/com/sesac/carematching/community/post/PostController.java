package com.sesac.carematching.community.post;

import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/community")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    /**
     * 현재 로그인한 사용자의 프로필 이미지, 닉네임, 작성글 수, 댓글 수, 좋아요 수 조회
     */
    @GetMapping("/user-info")
    public ResponseEntity<CommunityUserResponse> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        CommunityUserResponse userInfo = postService.getUserInfo(username);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 게시글 목록 조회
     * access = "ALL", "CAREGIVER", "USER"
     * page, size (무한 스크롤/페이징용)
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<CommunityPostListResponse>> getPosts(
        @RequestParam String access,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserInfo(username);

        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityPostListResponse> result = postService.getPostsByAccess(access, user, pageable);

        return ResponseEntity.ok(result);
    }

    /**
     * 인기글(좋아요 10개 이상) 조회
     * access = "ALL", "CAREGIVER", "USER"
     */
    @GetMapping("/popular-posts")
    public ResponseEntity<Page<CommunityPostListResponse>> getPopularPosts(
        @RequestParam String access,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserInfo(username);

        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityPostListResponse> result = postService.getPopularPosts(access, user, pageable);

        return ResponseEntity.ok(result);
    }

    /**
     * 게시글 검색
     * access = "ALL", "CAREGIVER", "USER"
     * keyword = 제목/내용 검색어
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CommunityPostListResponse>> searchPosts(
        @RequestParam String access,
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserInfo(username);

        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityPostListResponse> result = postService.searchPosts(access, user, keyword, pageable);

        return ResponseEntity.ok(result);
    }

    /**
     * (1) 내가 작성한 게시글 조회 (최신순)
     */
    @GetMapping("/my-posts")
    public ResponseEntity<Page<MyPostListResponse>> getMyPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 현재 로그인 사용자 정보
        User user = userService.getUserInfo(username);

        Pageable pageable = PageRequest.of(page, size);
        Page<MyPostListResponse> myPosts = postService.getMyPosts(user, pageable);

        return ResponseEntity.ok(myPosts);
    }
}
