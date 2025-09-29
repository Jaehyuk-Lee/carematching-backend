package com.sesac.carematching.elasticsearch.repository;

import com.sesac.carematching.elasticsearch.document.PostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Integer> {
    Page<PostDocument> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}
