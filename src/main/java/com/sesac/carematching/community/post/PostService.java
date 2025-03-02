package com.sesac.carematching.community.post;

import com.sesac.carematching.community.category.Category;
import com.sesac.carematching.community.category.CategoryRepository;
import com.sesac.carematching.community.comment.CommentRepository;
import com.sesac.carematching.community.like.LikeRepository;
import com.sesac.carematching.community.viewcount.ViewcountRepository;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final ViewcountRepository viewcountRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 사용자 정보 + 작성글 수, 댓글 수, 좋아요 수
     */
    public CommunityUserResponse getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        int postCount = postRepository.countByUser(user);
        int commentCount = commentRepository.countByUser(user);
        int likeCount = likeRepository.countByUser(user);

        return new CommunityUserResponse(user, postCount, commentCount, likeCount);
    }

    /**
     * access("ALL","CAREGIVER") 에 해당하는 카테고리의 게시글만 조회
     */
    public Page<CommunityPostListResponse> getPostsByAccess(String access, User user, Pageable pageable) {
        // 접근 권한 체크
        checkAccessRole(access, user);

        // 해당 access의 카테고리를 가져오되, 존재하지 않으면 예외
        Category category = getCategoryOrThrow(access);

        // 해당 카테고리 게시글 조회
        Page<Post> posts = postRepository.findByCategory(
                category,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        return posts.map(this::mapToResponse);
    }

    /**
     * 인기글 (좋아요 10개 이상)
     */
    public Page<CommunityPostListResponse> getPopularPosts(String access, User user, Pageable pageable) {
        checkAccessRole(access, user);

        Category category = getCategoryOrThrow(access);
        Page<Post> postPage = postRepository.findPopularPostsByCategory(category, pageable);

        return postPage.map(post -> {
            // in-memory로 likeCount, commentCount, viewCount 계산
            int likeCount = likeRepository.countByPost(post);
            int commentCount = commentRepository.countByPost(post);
            int viewCount = viewcountRepository.countByPost(post);

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
     * 검색 (제목/내용)
     */
    public Page<CommunityPostListResponse> searchPosts(String access, User user, String keyword, Pageable pageable) {
        checkAccessRole(access, user);

        Category category = getCategoryOrThrow(access);
        Page<Post> posts = postRepository.findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndContentContainingIgnoreCase(
                category, keyword,
                category, keyword,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        return posts.map(this::mapToResponse);
    }

    /**
     * Role 검사
     *  - CAREGIVER 접근은 ROLE_USER_CAREGIVER와 ROLE_ADMIN만 접근 가능
     *  - ALL 접근은 제한 없음
     */
    private void checkAccessRole(String access, User user) {
        // CAREGIVER 접근: ROLE_USER_CAREGIVER 혹은 ROLE_ADMIN
        if ("CAREGIVER".equalsIgnoreCase(access)
                && !( "ROLE_USER_CAREGIVER".equals(user.getRole().getRname())
                || "ROLE_ADMIN".equals(user.getRole().getRname()) )) {
            throw new RuntimeException("요양사 전용 카테고리입니다.");
        }

        // ALL 접근: 제한 없음
    }

    /**
     * [내가 작성한 게시글] 조회 (최신순)
     */
    public Page<MyPostListResponse> getMyPosts(User user, Pageable pageable) {
        Page<Post> postPage = postRepository.findByUser(
                user,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        return postPage.map(post -> {
            Category category = post.getCategory();

            int viewCount = viewcountRepository.countByPost(post);
            int likeCount = likeRepository.countByPost(post);
            int commentCount = commentRepository.countByPost(post);

            String relativeTime = getRelativeTime(post.getCreatedAt());
            return new MyPostListResponse(
                    post,
                    category,
                    relativeTime,
                    viewCount,
                    likeCount,
                    commentCount
            );
        });
    }

    /**
     * 카테고리 조회 (존재하지 않을 시 예외)
     */
    private Category getCategoryOrThrow(String access) {
        return categoryRepository.findByAccess(access)
                .orElseThrow(() -> new IllegalArgumentException("해당 access에 해당하는 카테고리가 존재하지 않습니다. access=" + access));
    }

    /**
     * Post -> CommunityPostListResponse 변환
     */
    private CommunityPostListResponse mapToResponse(Post post) {
        User writer = post.getUser();
        int viewCount = viewcountRepository.countByPost(post);
        int likeCount = likeRepository.countByPost(post);
        int commentCount = commentRepository.countByPost(post);

        String relativeTime = getRelativeTime(post.getCreatedAt());

        return new CommunityPostListResponse(
                post,
                writer,
                relativeTime,
                viewCount,
                likeCount,
                commentCount
        );
    }

    /**
     * "x분 전", "x시간 전", "x일 전" 형태의 상대 시간 계산
     */
    private String getRelativeTime(Instant createdAt) {
        Duration duration = Duration.between(createdAt, Instant.now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

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
            return (days / 30) + "개월 전";
        } else {
            return (days / 360) + "년 전";
        }
    }
}
