package com.sesac.carematching.util.fallback;

import com.sesac.carematching.config.aop.AspectOrdering;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Order(AspectOrdering.FALLBACK_CHECK)
public class FallbackCheckAspect {

    /**
     * @CircuitBreaker 어노테이션이 붙은 메서드가 값을 반환했을 때 실행됩니다.
     * 반환된 객체가 Fallbackable이고, 실제 fallback이 발생했다면 예외로 전환합니다.
     *
     * @param joinPoint      프록시된 메서드에 대한 정보
     * @param circuitBreaker 적용된 CircuitBreaker 어노테이션 정보
     * @param result         메서드가 반환한 결과 객체
     */
    @AfterReturning(
        pointcut = "@annotation(circuitBreaker)",
        returning = "result"
    )
    public void check(JoinPoint joinPoint, CircuitBreaker circuitBreaker, Object result) {
        if (result instanceof Fallbackable fallbackable) {

            if (fallbackable.isFallback()) {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                Method method = signature.getMethod();

                String circuitName = circuitBreaker.name();
                // 기본 설정
                String message = String.format("외부 API(%s) 호출에 실패했습니다. 잠시 후 다시 시도해주세요.", circuitName);
                int code = 503; // SERVICE_UNAVAILABLE

                // @FallbackMessage 어노테이션이 붙어 있다면 커스터마이징 적용
                FallbackMessage fallbackMessage = method.getAnnotation(FallbackMessage.class);
                if (fallbackMessage != null && fallbackMessage.message() != null) {
                    message = fallbackMessage.message();
                }
                if (fallbackMessage != null) {
                    code = fallbackMessage.code();
                }

                throw new ApiFallbackException(code, message);
            }
        } else {
            log.warn("CircuitBreaker '{}'가 적용된 메서드 '{}'의 반환 타입이 Fallbackable을 구현하지 않았습니다. Fallback 검사를 건너뜁니다.",
                circuitBreaker.name(),
                joinPoint.getSignature().toShortString());
        }
    }
}
