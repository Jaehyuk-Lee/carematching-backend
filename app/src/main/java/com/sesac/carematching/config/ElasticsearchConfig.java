package com.sesac.carematching.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.sesac.carematching.elasticsearch.repository")
public class ElasticsearchConfig {
}
