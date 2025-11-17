package com.sesac.carematching.transaction.payment.pendingPayment;

import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentReadyRequestDTO;
import com.sesac.carematching.transaction.payment.PaymentProvider;
import com.sesac.carematching.transaction.payment.PaymentServiceFactory;
import com.sesac.carematching.transaction.payment.provider.kakao.KakaoPayException;
import com.sesac.carematching.transaction.payment.provider.kakao.KakaoPayService;
import com.sesac.carematching.transaction.payment.provider.toss.TossPaymentService;
import com.sesac.carematching.transaction.payment.provider.toss.TossPaymentsException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * DB 상태와 관계없이 '능동적으로' PG사 API를 호출하여
 * HALF-OPEN 상태의 서킷을 테스트하고 복구를 시도하는 스케줄러.
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
        String circuitName = "TossPayments_Confirm";
        if (isCircuitNotClosed(circuitName)) {
            log.info("[ActiveCheck] TOSS Confirm 회로({}) 복구 테스트 시작...", circuitName);
            try {
                TossPaymentService tossService = (TossPaymentService) paymentServiceFactory.getService(PaymentProvider.TOSS);

                // 가짜(dummy) 데이터로 confirm API 호출
                PaymentConfirmRequestDTO dummyRequest = PaymentConfirmRequestDTO.builder()
                    .orderId("health-check-" + UUID.randomUUID())
                    .amount(100) // 실제 금액과 무관
                    .paymentKey("dummy-payment-key-for-health-check")
                    .build();

                // 이 호출이 @CircuitBreaker(name="TossPayments_Confirm")에 의해 가로채짐
                tossService.confirmPayment(dummyRequest);

                // (참고) 여기까지 코드가 도달했다면, PG가 4xx 오류를 반환했고
                // ignoreExceptions 설정 덕분에 서킷이 CLOSED로 전환되었음을 의미.
                log.info("[ActiveCheck] TOSS Confirm 회로가 4xx 응답을 반환. CLOSED로 간주.");

            } catch (TossPaymentsException e) {
                // 4xx 오류(TossPaymentsException)는 우리가 ignoreExceptions에 등록했기 때문에
                // 서킷을 CLOSED로 전환시킴. 따라서 이 예외는 정상으로 간주하고 무시.
                log.info("[ActiveCheck] TOSS Confirm 회로 복구 확인 (4xx): {}", e.getMessage());
            } catch (Exception e) {
                // 5xx, Timeout 등 'ignore'되지 않은 다른 모든 예외.
                // 이 예외들은 서킷이 다시 OPEN 상태로 돌아가게 만듦. (정상 동작)
                log.warn("[ActiveCheck] TOSS Confirm 회로 복구 테스트 실패 (5xx/Timeout): {}", e.getMessage());
            }
        }
    }

    /**
     * 1분마다 실행
     */
    @Scheduled(fixedDelayString = "PT1M")
    public void checkKakaoReady() {
        String circuitName = "KakaoPay_Ready";
        if (isCircuitNotClosed(circuitName)) {
            log.info("[ActiveCheck] KAKAO Ready 회로({}) 복구 테스트 시작...", circuitName);
            try {
                KakaoPayService kakaoService = (KakaoPayService) paymentServiceFactory.getService(PaymentProvider.KAKAO);

                // 가짜(dummy) 데이터로 ready API 호출
                PaymentReadyRequestDTO dummyRequest = new PaymentReadyRequestDTO();
                dummyRequest.setOrderId("health-check-" + UUID.randomUUID());
                dummyRequest.setUserId("health-check-user");
                dummyRequest.setItemName("Health Check");
                dummyRequest.setQuantity(1);
                dummyRequest.setTotalAmount(100);

                // 이 호출이 @CircuitBreaker(name="KakaoPay_Ready")에 의해 가로채짐
                kakaoService.readyPayment(dummyRequest);

            } catch (KakaoPayException e) {
                // 4xx 오류. 서킷 CLOSED로 전환됨.
                log.info("[ActiveCheck] KAKAO Ready 회로 복구 확인 (4xx): {}", e.getMessage());
            } catch (Exception e) {
                // 5xx, Timeout 등. 서킷 OPEN으로 전환됨.
                log.warn("[ActiveCheck] KAKAO Ready 회로 복구 테스트 실패 (5xx/Timeout): {}", e.getMessage());
            }
        }
    }

    // (필요시 KakaoPay_Confirm도 위와 동일하게 구현)

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
