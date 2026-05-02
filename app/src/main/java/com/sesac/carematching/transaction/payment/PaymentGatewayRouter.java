package com.sesac.carematching.transaction.payment;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class PaymentGatewayRouter {

    // 현재 활성화된 PG사
    @Getter
    private volatile PaymentProvider activeProvider;

    // 이용 가능한 PG사
    @Getter
    private final CopyOnWriteArraySet<PaymentProvider> availableProviders;

    public PaymentGatewayRouter() {
        this.availableProviders = new CopyOnWriteArraySet<>(EnumSet.allOf(PaymentProvider.class));
        // 어플리케이션 시작 시 기본값으로 TOSS 설정
        this.activeProvider = PaymentProvider.TOSS;
    }

    public void removeAvailableProvider(PaymentProvider provider) {
        this.availableProviders.remove(provider);
        refreshActiveProvider();
    }

    public void addAvailableProvider(PaymentProvider provider) {
        this.availableProviders.add(provider);
        refreshActiveProvider();
    }

    private synchronized void refreshActiveProvider() {
        PaymentProvider newProvider = null;

        // PaymentProvider에 선언된 값들의 순서를 기반으로 PG사 선정 우선순위 반영
        for (PaymentProvider provider : PaymentProvider.values()) {
            if (this.availableProviders.contains(provider)) {
                newProvider = provider;
                break;
            }
        }

        if (newProvider == null) {
            log.error("[CRITICAL] 사용 가능한 PG사가 없습니다. 결제 시스템이 중단됩니다.");
        }

        if (this.activeProvider != newProvider) {
            log.warn("[PG사 라우팅 변경] 이전: {} -> 신규: {}.", this.activeProvider, newProvider);
            this.activeProvider = newProvider;
        }
    }
}
