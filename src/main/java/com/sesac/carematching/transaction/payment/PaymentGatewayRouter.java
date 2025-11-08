package com.sesac.carematching.transaction.payment;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentGatewayRouter {

    // 현재 활성화된 PG사
    @Getter
    private volatile PaymentProvider activeProvider;

    public PaymentGatewayRouter() {
        // 어플리케이션 시작 시 기본값으로 TOSS 설정
        this.activeProvider = PaymentProvider.TOSS;
    }

    public synchronized void switchActiveProvider(PaymentProvider newProvider) {
        if (newProvider == null) {
            throw new IllegalArgumentException("PaymentProvider는 null일 수 없습니다.");
        }
        if (this.activeProvider != newProvider) {
            log.warn("[PG사 라우팅 변경] 이전: {} -> 신규: {}.", this.activeProvider, newProvider);
            this.activeProvider = newProvider;
        }
    }
}
