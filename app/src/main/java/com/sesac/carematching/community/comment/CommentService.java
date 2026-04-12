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

    /**
     * 댓글 등록
     */
    public CommentResponse createComment(User user, CommentRequest request) {
        // 게시글 조회 (게시글이 없으면 예외 발생)
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 새로운 댓글 엔티티 생성
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(request.getContent());
        comment.setIsAnonymous(request.isAnonymous());

        // 댓글 저장
        Comment savedComment = commentRepository.save(comment);

        // 새 댓글은 현재 로그인한 사용자가 작성했으므로 isAuthor 값은 true로 설정
        return new CommentResponse(savedComment, user, true);
    }

    /**
     * 게시글에 작성된 댓글 조회 (최신순, 페이징 처리)
     */
    public Page<CommentResponse> getCommentsByPost(Integer postId, User currentUser, Pageable pageable) {
        // 게시글 조회 (존재하지 않으면 예외 발생)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 게시글에 해당하는 댓글들을 페이지 단위로 조회
        Page<Comment> commentPage = commentRepository.findByPost(post, pageable);

        // 각 댓글마다 현재 사용자가 작성자인지 여부를 판단 후 CommentResponse로 변환
        return commentPage.map(comment -> {
            boolean isAuthor = comment.getUser().getId().equals(currentUser.getId());
            return new CommentResponse(comment, comment.getUser(), isAuthor);
        });
    }

    /**
     * 댓글 삭제
     * @param commentId 삭제할 댓글의 id
     * @param currentUser 현재 로그인한 사용자
     */
    public void deleteComment(Integer commentId, User currentUser) {
        // 댓글 조회 (존재하지 않으면 예외 발생)
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 댓글 작성자와 현재 사용자가 일치하는지 확인
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다.");
        }

        // 댓글 삭제
        commentRepository.delete(comment);
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
