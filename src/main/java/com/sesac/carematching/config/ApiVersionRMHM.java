package com.sesac.carematching.config;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * API 버전 기반 요청 매핑을 처리하는 핸들러 매핑 클래스
 * RequestMappingHandlerMapping을 확장하여 API 버전 정보를 URL 경로에 추가합니다.
 */
public class ApiVersionRMHM extends RequestMappingHandlerMapping {

    private final String prefix;

    public ApiVersionRMHM(String prefix) {
        this.prefix = prefix;
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(@NonNull Method method, @NonNull Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        if (info == null) {
            return null;
        }

        // 메서드 또는 클래스에서 ApiVersion 어노테이션 찾기
        ApiVersion methodAnnotation = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        ApiVersion typeAnnotation = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);

        // 원래 경로 패턴 가져오기
        String originalPattern = info.getPatternValues().iterator().next();
        Set<String> patterns = new HashSet<>();

        if (methodAnnotation == null && typeAnnotation == null) {
            // 어노테이션이 없는 경우, 모든 버전의 URL과 버전 없는 URL 처리
            patterns.add(originalPattern); // 버전 없는 URL

            // 모든 버전에 대한 URL 패턴 추가
            // v1, v2, ... 등 모든 버전 번호에 대응
            String versionPattern = prefix + "/v{version:\\d+}" + originalPattern.substring(prefix.length());
            patterns.add(versionPattern);
        } else {
            // 어노테이션이 있는 경우 기존 로직대로 처리
            int[] versions = methodAnnotation != null ? methodAnnotation.value() : typeAnnotation.value();
            patterns = getStrings(versions, originalPattern);
        }

        // 원래 RequestMappingInfo의 모든 설정을 유지하면서 경로만 변경
        return info.mutate()
            .paths(patterns.toArray(new String[0]))
            .build();
    }

    private Set<String> getStrings(int[] versions, String originalPattern) {
        Set<String> patterns = new HashSet<>();

        // 각 버전에 대한 경로 패턴 생성
        for (int version : versions) {
            String versionedPattern = prefix + "/v" + version + originalPattern.substring(prefix.length());
            patterns.add(versionedPattern);

            // v1인 경우 버전 없는 URL도 추가 (기본값으로 v1 사용)
            if (version == 1) {
                patterns.add(originalPattern);
            }
        }
        return patterns;
    }
}
