package com.sesac.carematching.community.viewcount;

import com.sesac.carematching.community.post.Post;
import com.sesac.carematching.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ViewcountService {

    private final ViewcountRepository viewcountRepository;

    /**
     * 사용자 단위로 게시글을 처음 볼 때만 기록을 남기고,
     * 이후에는 같은 사용자가 본 적 있으면 조회수를 증가시키지 않도록 처리
     */
    public void incrementViewCount(User user, Post post) {
        boolean alreadyViewed = viewcountRepository.existsByUserAndPost(user, post);
        if (!alreadyViewed) {
            Viewcount viewcount = new Viewcount();
            viewcount.setUser(user);
            viewcount.setPost(post);
            viewcount.setCreatedAt(Instant.now());
            viewcountRepository.save(viewcount);
        }
    }
}
