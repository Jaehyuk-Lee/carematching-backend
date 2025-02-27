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
import java.util.List;
import java.util.stream.Collectors;

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
     * access("ALL","CAREGIVER","USER") 에 해당하는 카테고리의 게시글만 조회
     */
    public Page<CommunityPostListResponse> getPostsByAccess(String access, User user, Pageable pageable) {
        // 접근 권한 체크 (예: "CAREGIVER" 인데 user의 Role이 ROLE_CAREGIVER 인지)
        checkAccessRole(access, user);

        // 해당 access의 카테고리를 찾음
        Category category = categoryRepository.findByAccess(access)
            .orElseThrow(() -> new IllegalArgumentException("해당 access에 해당하는 카테고리가 존재하지 않습니다."));

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
     * -> DB 레벨에서 "10개 이상"인 것만 조회 + 최신순 정렬 + 페이징
     */
    public Page<CommunityPostListResponse> getPopularPosts(String access, User user, Pageable pageable) {
        // 접근 권한 체크
        checkAccessRole(access, user);

        Page<Post> postPage;
        if ("ALL".equalsIgnoreCase(access)) {
            // 전체 카테고리라고 해서 "요양사"/"수급자"까지 합치는 게 아니라
            // 그냥 "ALL" 이라는 카테고리 자체가 있다면 그 카테고리로 조회하셨다면 이전과 다를 수 있음.
            // 하지만 문제에서 "ALL"이면 정말 '전체' 취급해야 한다면:
            //   -> findPopularPostsAll(...)
            postPage = postRepository.findPopularPostsAll(pageable);

        } else {
            // CAREGIVER or USER
            Category category = categoryRepository.findByAccess(access)
                .orElseThrow(() -> new IllegalArgumentException("해당 access 카테고리가 존재하지 않습니다."));
            postPage = postRepository.findPopularPostsByCategory(category, pageable);
        }

        // 이제 postPage에는 "좋아요 10개 이상"인 게시글만 있고
        // 최신순 + 페이징이 DB에서 이미 수행됨

        // 페이지 안의 각 Post를 DTO로 변환
        return postPage.map(post -> {
            // in-memory로 likeCount, commentCount, viewCount 세부 계산 가능
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
     * - 해당 access 카테고리에 속한 글만 검색
     */
    public Page<CommunityPostListResponse> searchPosts(String access, User user, String keyword, Pageable pageable) {
        checkAccessRole(access, user);

        Category category = categoryRepository.findByAccess(access)
            .orElseThrow(() -> new IllegalArgumentException("해당 access에 해당하는 카테고리가 존재하지 않습니다."));

        // JPA Derived Query 특성상 OR 조건을 쓰려면 메서드가 다소 복잡해질 수 있으므로
        // 아래처럼 Query Method를 정의해두었다고 가정합니다.
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
     *  - CAREGIVER 접근은 ROLE_CAREGIVER만
     *  - USER 접근은 ROLE_USER만
     *  - ALL 접근은 제한 없음
     */
    private void checkAccessRole(String access, User user) {
        if ("CAREGIVER".equalsIgnoreCase(access)
            && !"ROLE_CAREGIVER".equals(user.getRole().getRname())) {
            throw new RuntimeException("요양사 전용 카테고리입니다.");
        }
        if ("USER".equalsIgnoreCase(access)
            && !"ROLE_USER".equals(user.getRole().getRname())) {
            throw new RuntimeException("수급자 전용 카테고리입니다.");
        }
        // ALL -> 제한 없음
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
     * [내가 작성한 게시글] 조회 (최신순)
     * page, size 로 페이징
     */
    public Page<MyPostListResponse> getMyPosts(User user, Pageable pageable) {
        // 1) DB에서 user가 작성한 Post 목록을 최신순으로 조회
        Page<Post> postPage = postRepository.findByUser(
            user,
            PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
            )
        );

        // 2) Post -> MyPostListResponse 로 매핑
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

    // "x분 전", "x시간 전", "x일 전"
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
            return days/30 + "개월 전";
        } else {
            return days/360 + "년 전";
        }
    }

    /**
     * 필터링된 목록을 Page 형태로 변환 (무한 스크롤 대비)
     */
    private Page<CommunityPostListResponse> toPage(List<Post> filteredList, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredList.size());

        // 만약 start가 end보다 크다면 빈 목록
        List<Post> pageContent = (start > end)
            ? List.of()
            : filteredList.subList(start, end);

        List<CommunityPostListResponse> responseContent = pageContent.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return new PageImpl<>(responseContent, pageable, filteredList.size());
    }
}
