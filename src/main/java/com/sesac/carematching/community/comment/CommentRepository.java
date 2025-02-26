package com.sesac.carematching.community.comment;

import com.sesac.carematching.community.post.Post;
import com.sesac.carematching.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    int countByUser(User user);      // 사용자별 댓글수
    int countByPost(Post post);      // 게시글별 댓글수
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
}
