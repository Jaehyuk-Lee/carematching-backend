package com.sesac.carematching.community.like;

import com.sesac.carematching.community.post.Post;
import com.sesac.carematching.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;

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
}
