package com.sesac.carematching.config;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class WebConfig implements WebMvcConfigurer, WebMvcRegistrations {

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ApiVersionRMHM("/api");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // 모든 API 엔드포인트에 대해 CORS 설정
        .allowedOrigins(
                    "https://carematching.kro.kr", "http://carematching.kro.kr",
                    "http://localhost:3000", "http://localhost") // 허용할 출처
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메소드
                .allowedHeaders("*") // 허용할 헤더
                .allowCredentials(true); // 자격 증명 허용
    }
}
