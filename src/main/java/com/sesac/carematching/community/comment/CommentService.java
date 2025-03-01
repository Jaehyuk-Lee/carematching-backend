package com.sesac.carematching.community.comment;

import com.sesac.carematching.community.post.Post;
import com.sesac.carematching.community.post.PostRepository;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 생성
    public Comment createComment(Integer postId, String username, String content, boolean isAnonymous) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        comment.setIsAnonymous(isAnonymous);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        return commentRepository.save(comment);
    }

    // 댓글 삭제
    public void deleteComment(Integer commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 본인 댓글인지 확인
        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("본인 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }

    // 특정 게시글의 댓글 목록 조회
    public List<Comment> getCommentsByPost(Integer postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        return commentRepository.findByPostOrderByCreatedAtDesc(post);
    }

    // 사용자별 댓글 수
    public int countByUser(User user) {
        return commentRepository.countByUser(user);
    }

    /**
     * (2) 내가 작성한 댓글 조회 (최신순)
     */
    public Page<MyCommentListResponse> getMyComments(User user, Pageable pageable) {
        // 1) 내가 작성한 댓글들을 createdAt 내림차순 정렬
        Page<Comment> commentPage = commentRepository.findByUser(
            user,
            PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
            )
        );

        // 2) Comment -> MyCommentListResponse 로 변환
        return commentPage.map(comment -> {
            Post post = comment.getPost();
            String relativeTime = getRelativeTime(comment.getCreatedAt());

            return new MyCommentListResponse(post, comment, relativeTime);
        });
    }

    /**
     * 상대시간 계산
     */
    private String getRelativeTime(Instant createdAt) {
        long minutes = Duration.between(createdAt, Instant.now()).toMinutes();
        long hours = Duration.between(createdAt, Instant.now()).toHours();
        long days = Duration.between(createdAt, Instant.now()).toDays();

        if (minutes < 1) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else if (hours < 48) {
            return "어제";
        } else if (days < 30) {
            return days + "일 전";
        } else if (days < 360) {
            return days/30 + "개월 전";
        } else {
            return days/360 + "년 전";
        }
    }
}
