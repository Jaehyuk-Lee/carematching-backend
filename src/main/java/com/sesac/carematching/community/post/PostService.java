package com.sesac.carematching.community.post;

import com.sesac.carematching.community.category.Category;
import com.sesac.carematching.community.category.CategoryRepository;
import com.sesac.carematching.community.comment.CommentRepository;
import com.sesac.carematching.community.like.LikeRepository;
import com.sesac.carematching.community.viewcount.Viewcount;
import com.sesac.carematching.community.viewcount.ViewcountRepository;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import com.sesac.carematching.util.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final S3UploadService s3UploadService;

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

        Category category = categoryRepository.findByAccess(access)
            .orElseThrow(() -> new IllegalArgumentException("해당 access 카테고리가 존재하지 않습니다."));
        Page<Post> postPage = postRepository.findPopularPostsByCategory(category, pageable);

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
     * 게시글 생성
     */
    public CommunityPostListResponse createPost(CommunityPostRequest dto, String imageUrl, User user) {
        // 1) 카테고리 조회 (access = "ALL" / "CAREGIVER")
        Category category = categoryRepository.findByName(dto.getCategory())
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 name에 해당하는 카테고리가 존재하지 않습니다. name=" + dto.getCategory()));

        // 2) 권한 체크 (category.getAccess()와 user의 Role 비교)
        checkAccessRole(category.getAccess(), user);

        // 3) Post 엔티티 생성
        Post post = new Post();
        post.setUser(user);
        post.setCategory(category);
        post.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : false);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImage(imageUrl); // S3에서 업로드한 이미지 URL 저장

        // 4) DB 저장
        Post savedPost = postRepository.save(post);

        // 5) 저장된 Post를 DTO로 변환 후 반환
        return mapToResponse(savedPost);
    }

    /**
     * 게시글 상세 조회
     */
    public CommunityPostDetailResponse getPostDetail(Integer postId, User currentUser) {
        // 1) 게시글 조회 (없으면 예외)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

        // 2) 조회수 기록이 없으면 Viewcount 테이블에 추가 (첫 조회 시)
        boolean alreadyViewed = viewcountRepository.existsByUserAndPost(currentUser, post);
        if (!alreadyViewed) {
            Viewcount view = new Viewcount();
            view.setUser(currentUser);
            view.setPost(post);
            // createdAt은 AuditingEntityListener에 의해 자동 세팅
            viewcountRepository.save(view);
        }

        // 3) 조회수, 좋아요 수, 댓글 수 계산
        int viewCount = viewcountRepository.countByPost(post);
        int likeCount = likeRepository.countByPost(post);
        int commentCount = commentRepository.countByPost(post);

        // 4) 현재 사용자가 이 게시글을 좋아요 눌렀는지, 작성자인지
        boolean isLiked = likeRepository.findByUserAndPost(currentUser, post).isPresent();
        boolean isAuthor = post.getUser().getId().equals(currentUser.getId());

        // 5) CommunityPostDetailResponse 생성
        //    - 생성자에 필요한 값들을 넘김
        //    - 주의: DTO 생성자에 들어가는 user는 "게시글 작성자"여야 하므로, post.getUser()를 넘김
        return new CommunityPostDetailResponse(
                post,
                post.getUser(),      // 작성자 (DB에서 얕은 참조, N+1 문제 주의)
                viewCount,
                likeCount,
                commentCount,
                isLiked,
                isAuthor
        );
    }


    /**
     * 게시글 수정
     * - 새 이미지가 업로드된 경우 기존 이미지도 S3에서 삭제하고 새 URL로 교체
     */
    @Transactional
    public CommunityPostDetailResponse updatePost(
            Integer postId,
            CommunityPostRequest dto,   // 수정할 데이터(카테고리, 익명, 제목, 내용)
            String newImageUrl,         // 새로 업로드한 이미지 URL(없으면 null)
            User currentUser            // 수정 요청자
    ) {
        // 1) 기존 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

        // 2) 작성자 확인 (본인 글만 수정 가능)
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("본인이 작성한 글만 수정할 수 있습니다.");
        }

        // 3) 변경된 카테고리 조회 후 권한 체크
        Category category = categoryRepository.findByName(dto.getCategory())
                .orElseThrow(() ->
                        new IllegalArgumentException("해당 name에 해당하는 카테고리가 존재하지 않습니다. name=" + dto.getCategory()));
        checkAccessRole(category.getAccess(), currentUser);

        // 4) 게시글 엔티티 업데이트
        post.setCategory(category);
        post.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : false);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        // 새 이미지가 업로드된 경우: 기존 이미지가 있다면 S3에서 삭제 후 교체
        if (newImageUrl != null) {
            if (post.getImage() != null && !post.getImage().isEmpty()) {
                s3UploadService.deleteCommunityImageFile(post.getImage());
            }
            post.setImage(newImageUrl);
        }

        // 5) 업데이트된 post 기반으로 상세 DTO 생성 및 반환 (영속성 컨텍스트가 flush하며 UPDATE)
        int viewCount = viewcountRepository.countByPost(post);
        int likeCount = likeRepository.countByPost(post);
        int commentCount = commentRepository.countByPost(post);
        boolean isLiked = likeRepository.findByUserAndPost(currentUser, post).isPresent();
        boolean isAuthor = true; // 수정자는 작성자임

        return new CommunityPostDetailResponse(
                post,
                post.getUser(),
                viewCount,
                likeCount,
                commentCount,
                isLiked,
                isAuthor
        );
    }

    /**
     * 게시글 삭제
     * - 게시글 삭제 전에 S3에 업로드된 이미지가 있다면 해당 이미지도 삭제
     */
    @Transactional
    public void deletePost(Integer postId, User currentUser) {
        // 1) 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

        // 2) 작성자 확인 (본인 글만 삭제 가능)
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("본인이 작성한 글만 삭제할 수 있습니다.");
        }

        // 3) S3 이미지 삭제 (이미지 URL이 존재하면 삭제)
        if (post.getImage() != null && !post.getImage().isEmpty()) {
            s3UploadService.deleteCommunityImageFile(post.getImage());
        }

        // 4) DB에서 게시글 삭제
        postRepository.delete(post);
    }


    /**
     * Role 검사
     * - CAREGIVER 접근: ROLE_ADMIN, ROLE_USER_CAREGIVER만 접근 가능
     * - ALL 접근: 제한 없음
     */
    private void checkAccessRole(String access, User user) {
        // CAREGIVER 접근은 ROLE_ADMIN 이나 ROLE_USER_CAREGIVER만
        if ("CAREGIVER".equalsIgnoreCase(access)) {
            if (!("ROLE_ADMIN".equals(user.getRole().getRname())
                    || "ROLE_USER_CAREGIVER".equals(user.getRole().getRname()))) {
                throw new RuntimeException("요양사 전용 카테고리입니다.");
            }
        }

        // ALL 접근: 제한 없음
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
