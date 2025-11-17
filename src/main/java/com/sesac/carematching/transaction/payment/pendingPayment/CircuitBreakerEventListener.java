package com.sesac.carematching.transaction.payment.pendingPayment;

import com.sesac.carematching.transaction.payment.PaymentGatewayRouter;
import com.sesac.carematching.transaction.payment.PaymentProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
        String circuitName = event.getCircuitBreakerName();
        PaymentProvider provider = CIRCUIT_TO_PROVIDER_MAP.get(circuitName);

        // 관리 대상이 아닌 (PG와 무관한) 서킷은 무시
        if (provider == null) {
            log.debug("관리대상이 아닌 회로 이벤트 수신: {}", circuitName);
            return;
        }

        log.info("CircuitBreaker state transition: {} ({}) -> {}", circuitName, provider, event.getStateTransition());
        String transition = event.getStateTransition().toString();

        // [장애 감지] 하나라도 OPEN 되면 해당 PG 즉시 제외
        if (transition.endsWith("OPEN")) {
            log.warn("{} 회로 장애 감지 - {} 이용 제외", circuitName, provider);
            paymentGatewayRouter.removeAvailableProvider(provider);
        }
        // [복구 감지] 하나가 CLOSED 되면, 전체가 CLOSED인지 점검
        else if (transition.endsWith("CLOSED")) {
            log.info("{} 회로 복구 감지. {} PG의 전체 회로 상태 점검 시작...", circuitName, provider);

            if (isAllCircuitsForProviderClosed(provider)) {
                log.info("[전체 복구] {} PG의 모든 회로가 CLOSED 상태임을 확인. 서비스 라우팅에 추가합니다.", provider);
                paymentGatewayRouter.addAvailableProvider(provider);

                // PG가 완전히 복구되었으므로, 대기중인 결제 재시도
                // (Confirm이든 Ready든, PG가 '전체 복구'된 시점에 재시도하는 것이 안전)
                log.info("{} PG 서비스 복구됨 - Pending 결제 재시도 시작", provider);
                pendingPaymentRetryService.retryPendingPaymentsForProvider(provider);
            } else {
                log.info("[부분 복구] {} 회로는 복구되었으나, {} PG의 다른 회로가 아직 OPEN/HALF_OPEN 상태입니다. 서비스 라우팅 추가 보류.", circuitName, provider);
            }
        }
    }

    /**
     * 특정 PG사에 속한 모든 서킷 브레이커가 CLOSED 상태인지 확인합니다.
     *
     * @param provider 확인할 PG사
     * @return 모두 CLOSED 상태이면 true, 하나라도 아니면 false
     */
    private boolean isAllCircuitsForProviderClosed(PaymentProvider provider) {
        List<String> relatedCircuitNames = PROVIDER_TO_CIRCUITS_MAP.get(provider);

        if (relatedCircuitNames == null || relatedCircuitNames.isEmpty()) {
            log.warn("설정 오류: {} PG에 매핑된 회로가 없습니다. 복구 처리를 스킵합니다.", provider);
            return false; // 설정이 없으면 안전하게 '장애'로 처리
        }

        for (String circuitName : relatedCircuitNames) {
            try {
                CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(circuitName);
                CircuitBreaker.State currentState = cb.getState();

                // 하나라도 CLOSED 상태가 아니면, 즉시 false 반환
                if (currentState != CircuitBreaker.State.CLOSED) {
                    log.warn("...점검: {} 회로가 아직 {} 상태입니다.", circuitName, currentState);
                    return false;
                }
            } catch (Exception e) {
                // 설정(Map)에는 있으나 Registry에 실제 인스턴스가 없는 경우
                log.error("회로 '{}'를 Registry에서 찾는 중 오류 발생: {}", circuitName, e.getMessage());
                log.error("EventListener의 Map 설정을 확인해주세요. 존재하지 않는 회로 정보가 입력된 것으로 보입니다.");
                return false; // 찾을 수 없는 회로도 '장애'로 간주
            }
        }

        // 모든 회로가 CLOSED 상태임
        return true;
    }
}
