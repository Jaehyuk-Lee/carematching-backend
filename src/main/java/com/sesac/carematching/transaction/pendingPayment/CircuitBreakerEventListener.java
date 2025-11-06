package com.sesac.carematching.transaction.pendingPayment;

import com.sesac.carematching.transaction.PaymentProvider;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Resilience4j CircuitBreaker 상태 전이 이벤트를 수신하여,
 * 특정 회로가 CLOSED(복구)로 전환되면 해당 PG의 대기 결제 재시도를 트리거합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CircuitBreakerEventListener {
    private final PendingPaymentRetryService pendingPaymentRetryService;

    @Async
    @EventListener
    public void onStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        String name = event.getCircuitBreakerName();
        log.info("CircuitBreaker state transition: {} -> {}", name, event.getStateTransition());

        // 예: Toss 회로 이름은 "TossPayments_Confirm"으로 설정되어 있음
        if (event.getStateTransition().toString().endsWith("CLOSED")) {
            if ("TossPayments_Confirm".equals(name)) {
                log.info("TOSS 회로 복구 감지 - TOSS pending 결제 재시도 시작");
                pendingPaymentRetryService.retryPendingPaymentsForProvider(PaymentProvider.TOSS);
            } else if ("KakaoPay_Confirm".equals(name)) {
                log.info("KAKAO 회로 복구 감지 - KAKAO pending 결제 재시도 시작");
                pendingPaymentRetryService.retryPendingPaymentsForProvider(PaymentProvider.KAKAO);
            }
        }
    }
}
