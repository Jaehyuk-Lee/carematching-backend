package com.sesac.carematching.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 버전을 지정하기 위한 어노테이션
 * 컨트롤러 클래스나 메서드에 적용할 수 있습니다.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    /**
     * API 버전 배열
     * 예: {1, 2} - API v1과 v2 모두에서 사용 가능
     */
    int[] value();
}
