package com.sesac.carematching.community.post;

import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import com.sesac.carematching.util.S3UploadService;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/community")
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final TokenAuth tokenAuth;
    private final S3UploadService s3UploadService;
    private final PostRepository postRepository;

    /**
     * 현재 로그인한 사용자의 프로필 이미지, 닉네임, 작성글 수, 댓글 수, 좋아요 수 조회
     */
    @GetMapping("/user-info")
    public ResponseEntity<CommunityUserResponse> getUserInfo(HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        CommunityUserResponse userInfo = postService.getUserInfo(username);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 게시글 목록 조회
     * access = "ALL", "CAREGIVER"
     * page, size (무한 스크롤/페이징용)
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<CommunityPostListResponse>> getPosts(
        HttpServletRequest request,
        @RequestParam String access,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        String username = tokenAuth.extractUsernameFromToken(request);
        User user = userService.getUserInfo(username);

        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityPostListResponse> result = postService.getPostsByAccess(access, user, pageable);

        return ResponseEntity.ok(result);
    }

    /**
     * 인기글(좋아요 10개 이상) 조회
     * access = "ALL", "CAREGIVER"
     */
    @GetMapping("/popular-posts")
    public ResponseEntity<Page<CommunityPostListResponse>> getPopularPosts(
        HttpServletRequest request,
        @RequestParam String access,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        String username = tokenAuth.extractUsernameFromToken(request);
        User user = userService.getUserInfo(username);

        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityPostListResponse> result = postService.getPopularPosts(access, user, pageable);

        return ResponseEntity.ok(result);
    }

    /**
     * 게시글 검색
     * access = "ALL", "CAREGIVER"
     * keyword = 제목/내용 검색어
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CommunityPostListResponse>> searchPosts(
        HttpServletRequest request,
        @RequestParam String access,
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        String username = tokenAuth.extractUsernameFromToken(request);
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
        HttpServletRequest request,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        String username = tokenAuth.extractUsernameFromToken(request);

        // 현재 로그인 사용자 정보
        User user = userService.getUserInfo(username);

        Pageable pageable = PageRequest.of(page, size);
        Page<MyPostListResponse> myPosts = postService.getMyPosts(user, pageable);

        return ResponseEntity.ok(myPosts);
    }

    /**
     * 게시글 작성
     */
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommunityPostListResponse> createPost(
            HttpServletRequest request,
            @RequestPart("postRequest") @Valid CommunityPostRequest postRequest, // 게시글 정보(JSON)
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile // 이미지 파일(Optional)
    ) throws IOException {
        // 1) 토큰에서 사용자 정보 추출
        String username = tokenAuth.extractUsernameFromToken(request);
        User user = userService.getUserInfo(username);

        // 2) 이미지 업로드 처리
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = s3UploadService.saveFile(imageFile); // S3 업로드 후 URL 반환
        }

        // 3) 게시글 생성 서비스 호출
        CommunityPostListResponse createdPost = postService.createPost(postRequest, imageUrl, user);

        // 4) 생성된 게시글 정보 반환
        return ResponseEntity.ok(createdPost);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<CommunityPostDetailResponse> getPostDetail(
            HttpServletRequest request,
            @PathVariable Integer postId
    ) {
        // 1) 토큰에서 사용자 정보 추출
        String username = tokenAuth.extractUsernameFromToken(request);
        User user = userService.getUserInfo(username);

        // 2) PostService에서 상세조회 로직
        CommunityPostDetailResponse detail = postService.getPostDetail(postId, user);

        return ResponseEntity.ok(detail);
    }

    /**
     * 게시글 수정 전 기본 데이터 조회
     */
    @GetMapping("/posts/{postId}/update")
    public ResponseEntity<CommunityPostUpdateResponse> getPostForEdit(
            HttpServletRequest request,
            @PathVariable Integer postId
    ) {
        // 1) 현재 사용자
        String username = tokenAuth.extractUsernameFromToken(request);
        User user = userService.getUserInfo(username);

        // 2) 게시글 가져오기
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

        // 3) 게시글의 작성자와 현재 사용자가 같은지 검사 (본인 글만 수정 가능)
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        // 4) 필요한 항목만 담은 DTO 생성
        CommunityPostUpdateResponse response = new CommunityPostUpdateResponse(
                post.getCategory().getName(), // "ALL" / "CAREGIVER"
                post.getIsAnonymous(),
                post.getTitle(),
                post.getContent(),
                post.getImage() // S3에 업로드된 이미지 URL
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 수정
     */
    @PostMapping(value = "/posts/{postId}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommunityPostDetailResponse> updatePost(
            HttpServletRequest request,
            @PathVariable Integer postId,
            @RequestPart("postRequest") @Valid CommunityPostRequest postRequest, // 수정할 게시글 정보
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile // 새 이미지 파일(Optional)
    ) throws IOException {
        // 1) 토큰에서 사용자 정보 추출
        String username = tokenAuth.extractUsernameFromToken(request);
        User user = userService.getUserInfo(username);

        // 2) 새 이미지 업로드 처리 (있다면)
        String newImageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            newImageUrl = s3UploadService.saveFile(imageFile);
            // 필요하다면 이전 이미지를 S3에서 삭제할 수도 있음
        }

        // 3) 게시글 수정 Service 호출
        //    수정 후, 상세 화면에 필요한 정보(CommunityPostDetailResponse)를 반환
        CommunityPostDetailResponse updatedDetail =
                postService.updatePost(postId, postRequest, newImageUrl, user);

        return ResponseEntity.ok(updatedDetail);
    }

    /**
     * 게시글 삭제
     */
    @PostMapping("/posts/{postId}/delete")
    public ResponseEntity<String> deletePost(
            HttpServletRequest request,
            @PathVariable Integer postId
    ) {
        // 1) 토큰에서 사용자 정보 추출
        String username = tokenAuth.extractUsernameFromToken(request);
        User user = userService.getUserInfo(username);

        // 2) 게시글 삭제 서비스 호출
        postService.deletePost(postId, user);

        // 3) 삭제 성공 메시지 (또는 다른 DTO) 반환
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
    }



}
