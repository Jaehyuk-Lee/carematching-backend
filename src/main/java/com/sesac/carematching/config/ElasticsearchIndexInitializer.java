package com.sesac.carematching.config;

import com.sesac.carematching.elasticsearch.document.PostES;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;
import org.springframework.data.elasticsearch.core.document.Document;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ElasticsearchIndexInitializer implements ApplicationRunner {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchIndexProperties indexProperties; // 설정 주입

    // 인덱싱이 필요한 문서 클래스는 이 리스트에 추가
    private final List<Class<?>> documentClasses = List.of(
            PostES.class
    );

    @Override
    public void run(ApplicationArguments args) {
        for (Class<?> documentClass : documentClasses) {
            IndexOperations indexOps = elasticsearchOperations.indexOps(documentClass);

            if (!indexOps.exists()) {
                log.info("Elasticsearch index for '{}' does not exist. Creating index...", documentClass.getSimpleName());

                // 설정 파일(properties)에서 값을 읽어와 동적으로 설정
                Document settings = Document.create();
                settings.put("number_of_shards", indexProperties.getShards());
                settings.put("number_of_replicas", indexProperties.getReplicas());

                indexOps.create(settings);
                indexOps.putMapping(indexOps.createMapping(documentClass));
                log.info("Elasticsearch index and mapping for '{}' created successfully with {} shards and {} replicas.",
                        documentClass.getSimpleName(), indexProperties.getShards(), indexProperties.getReplicas());
            } else {
                log.info("Elasticsearch index for '{}' already exists.", documentClass.getSimpleName());
            }
        }
    }
}
