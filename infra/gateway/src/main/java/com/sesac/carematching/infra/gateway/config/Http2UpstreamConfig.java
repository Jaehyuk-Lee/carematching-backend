package com.sesac.carematching.infra.gateway.config;

import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.HttpProtocol;

@Configuration
public class Http2UpstreamConfig {

    @Bean
    public HttpClientCustomizer http2ClientCustomizer() {
        return httpClient -> httpClient.protocol(HttpProtocol.H2C);
    }
}
