package com.sesac.carematching.community.post;

import com.sesac.carematching.community.category.Category;
import com.sesac.carematching.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer> {

    // 작성글 개수
    int countByUser(User user);

    // 내가 작성한 글 페이지 조회
    Page<Post> findByUser(User user, Pageable pageable);

    // 특정 카테고리에 속한 게시글 페이징
    Page<Post> findByCategory(Category category, Pageable pageable);

    // 제목/내용 검색 (카테고리 구분 없이 전체)
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content, Pageable pageable);

    // 제목/내용 검색 (특정 카테고리)
    //  -> (category = ?1 AND (title LIKE ?2 OR content LIKE ?2)) 형태의 derived query가 필요
    //  아래와 같이 Or 조건을 추가해두면, JPA가 적절히 쿼리를 생성합니다.
    Page<Post> findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndContentContainingIgnoreCase(
            Category category1, String titleKeyword,
            Category category2, String contentKeyword,
            Pageable pageable
    );
}
