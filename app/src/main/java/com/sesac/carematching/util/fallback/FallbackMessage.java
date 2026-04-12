package com.sesac.carematching.util.fallback;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * FallbackCheckAspect가 사용할 커스텀 예외 메시지를 지정합니다.
 * 이 어노테이션이 없으면, Aspect는 기본 메시지를 생성합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FallbackMessage {
    String message();
    int code() default 503;
}
