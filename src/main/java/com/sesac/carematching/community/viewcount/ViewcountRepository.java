package com.sesac.carematching.community.viewcount;

import com.sesac.carematching.community.post.Post;
import com.sesac.carematching.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewcountRepository extends JpaRepository<Viewcount, Integer> {
    int countByPost(Post post);
    boolean existsByUserAndPost(User user, Post post);
}
