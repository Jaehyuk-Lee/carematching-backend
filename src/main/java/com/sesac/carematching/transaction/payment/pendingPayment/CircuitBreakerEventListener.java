package com.sesac.carematching.transaction.payment.pendingPayment;

import com.sesac.carematching.transaction.payment.PaymentGatewayRouter;
import com.sesac.carematching.transaction.payment.PaymentProvider;
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
public class CircuitBreakerEventListener implements ApplicationRunner {
    private final PendingPaymentRetryService pendingPaymentRetryService;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * 서킷 브레이커 이름 -> PaymentProvider 매핑
     * "어떤 서킷"이 "어떤 PG사"에 속하는지 정의합니다.
     */
    private static final Map<String, PaymentProvider> CIRCUIT_TO_PROVIDER_MAP = Map.of(
        "TossPayments_Confirm", PaymentProvider.TOSS,
        "KakaoPay_Confirm", PaymentProvider.KAKAO,
        "KakaoPay_Ready", PaymentProvider.KAKAO
    );

    /**
     * PaymentProvider -> 해당 PG사에 속한 모든 서킷 브레이커 이름 리스트 매핑
     * "어떤 PG사"가 "어떤 서킷들"로 구성되는지 정의합니다.
     */
    private static final Map<PaymentProvider, List<String>> PROVIDER_TO_CIRCUITS_MAP = Map.of(
        PaymentProvider.TOSS, List.of("TossPayments_Confirm"),
        PaymentProvider.KAKAO, List.of("KakaoPay_Confirm", "KakaoPay_Ready")
    );

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // CircuitBreakerRegistry에 등록된 모든 CircuitBreaker에 대해 이벤트 리스너 등록
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            circuitBreaker.getEventPublisher()
                .onStateTransition(this::onStateTransition);
            log.info("CircuitBreaker 이벤트 리스너 등록 완료: {}", circuitBreaker.getName());
        });
    }

    public void onStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        String name = event.getCircuitBreakerName();
        log.info("CircuitBreaker state transition: {} -> {}", name, event.getStateTransition());

        // 예: Toss 회로 이름은 "TossPayments_Confirm"으로 설정되어 있음
        if (event.getStateTransition().toString().endsWith("CLOSED")) {
            if ("TossPayments_Confirm".equals(name)) {
                log.info("TOSS 회로 복구 감지 - TOSS pending 결제 재시도 시작");
                pendingPaymentRetryService.retryPendingPaymentsForProvider(PaymentProvider.TOSS);
                paymentGatewayRouter.addAvailableProvider(PaymentProvider.TOSS);
            } else if ("KakaoPay_Confirm".equals(name)) {
                log.info("KAKAO 회로 복구 감지 - KAKAO pending 결제 재시도 시작");
                pendingPaymentRetryService.retryPendingPaymentsForProvider(PaymentProvider.KAKAO);
                paymentGatewayRouter.addAvailableProvider(PaymentProvider.KAKAO);
            }
        }

        if (event.getStateTransition().toString().endsWith("OPEN")) {
            if ("TossPayments_Confirm".equals(name)) {
                log.info("TOSS 회로 장애 감지 - TOSS 이용 제외");
                paymentGatewayRouter.removeAvailableProvider(PaymentProvider.TOSS);
            } else if ("KakaoPay_Confirm".equals(name)) {
                log.info("KAKAO 회로 장애 감지 - KAKAO 이용 제외");
                paymentGatewayRouter.removeAvailableProvider(PaymentProvider.KAKAO);
            }
        }
    }
}
