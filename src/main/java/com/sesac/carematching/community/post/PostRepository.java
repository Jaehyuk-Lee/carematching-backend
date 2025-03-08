package com.sesac.carematching.community.post;

import com.sesac.carematching.community.category.Category;
import com.sesac.carematching.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    /**
     * (A) 특정 카테고리에서 좋아요 10개 이상인 게시글
     *     -> createdAt DESC로 정렬 + 페이징
     *
     * JOIN p.likes l : Post와 Like를 조인
     * GROUP BY p     : 각 게시글별로 그룹화
     * HAVING COUNT(l) >= 10 : 좋아요 개수가 10개 이상인 경우
     * ORDER BY p.createdAt DESC : 최신순
     */
    @Query("""
       SELECT p
       FROM Post p
       JOIN p.likes l
       WHERE p.category = :category
       GROUP BY p
       HAVING COUNT(l) >= 10
       ORDER BY p.createdAt DESC
       """)
    Page<Post> findPopularPostsByCategory(Category category, Pageable pageable);

    long countByImage(String image);
}
