package com.sesac.carematching.community.comment;

import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import com.sesac.carematching.util.TokenAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Tag(name = "Comment Controller", description = "커뮤니티 댓글 관리")
@RestController
@RequestMapping("/api/community")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final TokenAuth tokenAuth;

    @Operation(summary = "댓글 등록", description = "게시글에 댓글을 등록합니다.")
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

    @Operation(summary = "게시글 댓글 목록 조회", description = "게시글 ID 기준으로 댓글 목록을 페이징 조회합니다.")
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

    @Operation(summary = "댓글 삭제", description = "댓글 ID로 댓글을 삭제합니다.")
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

    @Operation(summary = "내가 작성한 댓글 목록 조회", description = "로그인한 사용자가 작성한 댓글을 페이징 조회합니다.")
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
