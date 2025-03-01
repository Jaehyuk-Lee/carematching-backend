package com.sesac.carematching.community.comment;

import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
