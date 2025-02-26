package com.sesac.carematching.community.like;

import com.sesac.carematching.community.post.Post;
import com.sesac.carematching.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    int countByUser(User user);      // 해당 사용자가 눌렀던 좋아요 수
    int countByPost(Post post);      // 해당 게시글에 달린 좋아요 수
    Optional<Like> findByUserAndPost(User user, Post post);
}
