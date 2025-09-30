package com.sesac.carematching.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "elasticsearch.index")
public class ElasticsearchIndexProperties {
    /**
     * 인덱스의 샤드 수
     */
    private int shards = 1;
    /**
     * 인덱스의 복제본 수
     */
    private int replicas = 0;
}
