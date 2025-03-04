package com.sesac.carematching.community.like;

import com.sesac.carematching.community.comment.CommentRepository;
import com.sesac.carematching.community.post.CommunityPostListResponse;
import com.sesac.carematching.community.post.Post;
import com.sesac.carematching.community.viewcount.ViewcountRepository;
import com.sesac.carematching.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final ViewcountRepository viewcountRepository;
    private final CommentRepository commentRepository;

    // 게시글에 대한 전체 좋아요 개수
    public int countLikesForPost(Post post) {
        return likeRepository.countByPost(post);
    }

    // 좋아요 토글 (이미 눌렀으면 취소, 안 눌렀으면 등록)
    public void toggleLike(User user, Post post) {
        Like existingLike = likeRepository.findByUserAndPost(user, post).orElse(null);

        if (existingLike != null) {
            // 이미 좋아요를 누른 상태 -> 좋아요 취소
            likeRepository.delete(existingLike);
        } else {
            // 좋아요 추가
            Like newLike = new Like();
            newLike.setUser(user);
            newLike.setPost(post);
            newLike.setCreatedAt(Instant.now());
            likeRepository.save(newLike);
        }
    }

    /**
     * (3) 내가 좋아요한 게시글 조회
     *  - "좋아요 누른 시간"(Like.createdAt) 기준으로 최신순 정렬
     *  - relativeTime은 게시글 등록 시간(post.getCreatedAt()) 기준으로 계산
     */
    public Page<CommunityPostListResponse> getMyLikedPosts(User user, Pageable pageable) {

        // 1) DB에서 user가 누른 Like들을 createdAt DESC로 페이징 조회
        Page<Like> likePage = likeRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        // 2) 각 Like로부터 Post를 얻어, CommunityPostListResponse로 변환
        return likePage.map(like -> {
            Post post = like.getPost();  // 좋아요한 게시글
            int viewCount = viewcountRepository.countByPost(post);
            int likeCount = likeRepository.countByPost(post);
            int commentCount = commentRepository.countByPost(post);

            // relativeTime = "게시글 등록 시점" 기준
            String relativeTime = getRelativeTime(post.getCreatedAt());

            return new CommunityPostListResponse(
                post,
                post.getUser(),
                relativeTime,
                viewCount,
                likeCount,
                commentCount
            );
        });
    }

    /**
     * 좋아요 상태 업데이트
     * @param user 현재 로그인한 사용자
     * @param post 대상 게시글
     * @param isLiked true이면 삭제(좋아요 취소), false이면 추가(좋아요 등록)
     */
    public void updateLikeStatus(User user, Post post, boolean isLiked) {
        Optional<Like> existingLikeOpt = likeRepository.findByUserAndPost(user, post);
        if (isLiked) {
            // 현재 좋아요 상태이면 삭제 (취소)
            existingLikeOpt.ifPresent(like -> likeRepository.delete(like));
        } else {
            // 현재 좋아요가 아니라면 추가
            if (existingLikeOpt.isEmpty()) {
                Like newLike = new Like();
                newLike.setUser(user);
                newLike.setPost(post);
                newLike.setCreatedAt(Instant.now());
                likeRepository.save(newLike);
            }
        }
    }

    /**
     * x분 전, x시간 전, x일 전 계산 (게시글 등록 시점 기준)
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
