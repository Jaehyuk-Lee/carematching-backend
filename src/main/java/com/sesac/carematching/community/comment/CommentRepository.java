package com.sesac.carematching.community.comment;

import com.sesac.carematching.community.post.Post;
import com.sesac.carematching.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    int countByUser(User user);      // 사용자별 댓글수
    int countByPost(Post post);      // 게시글별 댓글수
    Page<Comment> findByPost(Post post, Pageable pageable);     // 게시글별 댓글 목록을 페이지 단위로 조회 (최신순 정렬)
    Page<Comment> findByUser(User user, Pageable pageable);     // 내가 작성한 댓글 목록 (페이지네이션, 최신순)
}
