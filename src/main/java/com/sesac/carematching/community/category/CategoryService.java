package com.sesac.carematching.community.category;

import com.sesac.carematching.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * access("ALL", "CAREGIVER", "USER") 값으로 카테고리를 찾아옴
     */
    public Category getCategoryByAccess(String access) {
        return categoryRepository.findByAccess(access)
            .orElseThrow(() -> new IllegalArgumentException("해당 access에 해당하는 카테고리가 존재하지 않습니다."));
    }

    /**
     * 사용자 권한(Role)에 따른 접근 가능 여부 검사
     * - CAREGIVER -> ROLE_CAREGIVER만 접근 가능
     * - USER -> ROLE_USER만 접근 가능
     * - ALL -> 제한 없음
     */
    public void checkCategoryAccess(User user, String access) {
        if ("CAREGIVER".equalsIgnoreCase(access)) {
            // '요양사' 카테고리는 ROLE_CAREGIVER만 접근
            if (!"ROLE_CAREGIVER".equals(user.getRole().getRname())) {
                throw new RuntimeException("요양사 전용 카테고리입니다.");
            }
        } else if ("USER".equalsIgnoreCase(access)) {
            // '수급자' 카테고리는 ROLE_USER만 접근
            if (!"ROLE_USER".equals(user.getRole().getRname())) {
                throw new RuntimeException("수급자 전용 카테고리입니다.");
            }
        }
        // ALL은 아무나 접근 가능하므로 별도 체크 없음
    }

    /**
     * 모든 카테고리 목록 조회
     * (필요 시 사용)
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
