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

        // 어노테이션이 없으면 기본 매핑 정보 반환
        if (methodAnnotation == null && typeAnnotation == null) {
            return info;
        }

        // 메서드 어노테이션이 있으면 메서드 어노테이션 사용, 없으면 클래스 어노테이션 사용
        int[] versions = methodAnnotation != null ? methodAnnotation.value() : typeAnnotation.value();

        // 원래 경로 패턴 가져오기
        String originalPattern = info.getPatternValues().iterator().next();

        Set<String> patterns = getStrings(versions, originalPattern);

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
