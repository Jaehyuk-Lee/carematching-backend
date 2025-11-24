package com.sesac.carematching.transaction.payment.pendingPayment;

import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentReadyRequestDTO;
import com.sesac.carematching.transaction.payment.PaymentProvider;
import com.sesac.carematching.transaction.payment.PaymentService;
import com.sesac.carematching.transaction.payment.PaymentServiceFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * DB 상태와 관계없이 능동적으로 PG사 API를 호출하여
 * HALF-OPEN 상태의 서킷을 테스트하고 복구를 시도하는 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivePgHealthChecker {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final PaymentServiceFactory paymentServiceFactory;

    /**
     * 1분마다 실행 (application.yaml의 waitDurationInOpenState 값과 유사하게 설정)
     */
    @Scheduled(fixedDelayString = "PT1M")
    public void checkTossConfirm() {
        performHealthCheck("TossPayments_Confirm", () -> {
            PaymentService paymentService = paymentServiceFactory.getService(PaymentProvider.TOSS);
            PaymentConfirmRequestDTO dummyRequest = new PaymentConfirmRequestDTO(
                "health-check-" + UUID.randomUUID(),
                100,
                "dummy-payment-key-for-health-check",
                null,
                null
            );
            paymentService.healthCheckConfirm(dummyRequest);
        });
    }

    /**
     * 1분마다 실행
     */
    @Scheduled(fixedDelayString = "PT1M")
    public void checkKakaoReady() {
        performHealthCheck("KakaoPay_Ready", () -> {
            PaymentService paymentService = paymentServiceFactory.getService(PaymentProvider.KAKAO);
            PaymentReadyRequestDTO dummyRequest = new PaymentReadyRequestDTO(
                "health-check-" + UUID.randomUUID(),
                "health-check-user",
                "Health Check",
                1,
                100
                );
            paymentService.healthCheckReady(dummyRequest);
        });
    }

    /**
     * 1분마다 실행
     */
    @Scheduled(fixedDelayString = "PT1M")
    public void checkKakaoConfirm() {
        performHealthCheck("KakaoPay_Confirm", () -> {
            PaymentService paymentService = paymentServiceFactory.getService(PaymentProvider.KAKAO);
            PaymentConfirmRequestDTO dummyRequest = new PaymentConfirmRequestDTO(
                "health-check-" + UUID.randomUUID(),
                100,
                "dummy-tid",
                "health-check-user",
                "dummy-pg-token"
            );
            paymentService.healthCheckConfirm(dummyRequest);
        });
    }

    private void performHealthCheck(String circuitName, Runnable healthCheckAction) {
        if (isCircuitNotClosed(circuitName)) {
            log.info("[ActiveCheck] {} 회로({}) 복구 테스트 시작...", circuitName.split("_")[0], circuitName);
            try {
                healthCheckAction.run();
                // (참고) 여기까지 코드가 도달했다면, PG가 4xx 오류를 반환
                log.info("[ActiveCheck] {} 회로가 4xx 응답을 반환. CLOSED로 간주.", circuitName);
            } catch (Exception e) {
                // 5xx, Timeout 등. 서킷 OPEN으로 전환됨.
                log.warn("[ActiveCheck] {} 회로 복구 테스트 실패 (5xx/Timeout): {}", circuitName, e.getMessage());
            }
        }
    }

    private boolean isCircuitNotClosed(String circuitName) {
        try {
            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(circuitName);
            CircuitBreaker.State state = cb.getState();
            // CLOSED가 아니면 (OPEN 또는 HALF_OPEN) 테스트 필요
            return state != CircuitBreaker.State.CLOSED;
        } catch (Exception e) {
            log.warn("회로 '{}' 상태 조회 실패: {}", circuitName, e.getMessage());
            return true; // 조회 실패 시에도 안전하게 테스트 시도
        }
    }
}
