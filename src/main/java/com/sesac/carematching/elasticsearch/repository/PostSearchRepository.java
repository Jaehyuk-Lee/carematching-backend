package com.sesac.carematching.elasticsearch.repository;

import com.sesac.carematching.elasticsearch.document.PostES;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostES, Integer> {
    Page<PostES> findByCategoryAccessAndTitleContainingIgnoreCaseOrCategoryAccessAndContentContainingIgnoreCase(
            String categoryAccess, String title, String categoryAccess2, String content, Pageable pageable
    );
}
