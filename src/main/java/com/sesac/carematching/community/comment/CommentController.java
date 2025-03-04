package com.sesac.carematching.community.comment;

import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/community")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final TokenAuth tokenAuth;

    /**
     * 댓글 등록 API
     */
    @PostMapping("/comment/add")
    public ResponseEntity<CommentResponse> createComment(
            HttpServletRequest request,
            @RequestBody CommentRequest commentRequest
    ) {
        // 토큰에서 사용자 이름 추출 후 사용자 정보 조회
        String username = tokenAuth.extractUsernameFromToken(request);
        User user = userService.getUserInfo(username);

        // 댓글 등록 처리 후 응답 DTO 생성
        CommentResponse response = commentService.createComment(user, commentRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 댓글 조회 API (게시글 id 기준, 10개씩 페이징 처리)
     */
    @GetMapping("/comments")
    public ResponseEntity<Page<CommentResponse>> getCommentsByPost(
            HttpServletRequest request,
            @RequestParam Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 토큰에서 사용자 이름 추출 후 사용자 정보 조회
        String username = tokenAuth.extractUsernameFromToken(request);
        User user = userService.getUserInfo(username);

        // 페이지 정보 생성 (최신순 정렬)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 게시글의 댓글들을 조회
        Page<CommentResponse> commentResponses = commentService.getCommentsByPost(postId, user, pageable);

        return ResponseEntity.ok(commentResponses);
    }

    /**
     * 댓글 삭제 API
     * 프론트로부터 댓글 id를 받아 삭제를 처리합니다.
     */
    @PostMapping("/comment/delete")
    public ResponseEntity<String> deleteComment(
            HttpServletRequest request,
            @RequestParam Integer commentId
    ) {

        // 토큰에서 사용자 이름 추출 후 사용자 정보 조회
        String username = tokenAuth.extractUsernameFromToken(request);
        User user = userService.getUserInfo(username);

        // 댓글 삭제 처리
        commentService.deleteComment(commentId, user);

        return ResponseEntity.ok("댓글 삭제가 완료되었습니다.");
    }

    /**
     * (2) 내가 작성한 댓글 조회 (최신순)
     */
    @GetMapping("/my-comments")
    public ResponseEntity<Page<MyCommentListResponse>> getMyComments(
        HttpServletRequest request,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        String username = tokenAuth.extractUsernameFromToken(request);

        User user = userService.getUserInfo(username);

        Pageable pageable = PageRequest.of(page, size);
        Page<MyCommentListResponse> myComments = commentService.getMyComments(user, pageable);

        return ResponseEntity.ok(myComments);
    }
}
