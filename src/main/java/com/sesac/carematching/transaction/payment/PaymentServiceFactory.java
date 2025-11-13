package com.sesac.carematching.transaction.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PaymentServiceFactory {

    private final Map<PaymentProvider, PaymentService> paymentServices;

    public PaymentServiceFactory(List<PaymentService> services) {

        // PG사 Enum을 키로 사용하는 EnumMap 생성
        this.paymentServices = new EnumMap<>(PaymentProvider.class);

        for (PaymentService service : services) {
            PaymentProvider providerType = service.getPaymentProvider();

            // 중복 등록 방지
            if (this.paymentServices.containsKey(providerType)) {
                throw new IllegalStateException("중복된 PG사 서비스가 등록되었습니다: " + providerType);
            }

            // 맵에 PG사와 서비스 인스턴스 수동 매핑
            this.paymentServices.put(providerType, service);
        }

        validateAllProvidersMapped();
    }

    // 외부에서 PG사에 맞는 서비스를 가져갈 때 사용
    public PaymentService getService(PaymentProvider provider) {
        PaymentService service = paymentServices.get(provider);
        if (service == null) {
            throw new IllegalArgumentException(provider + "에 해당하는 결제 서비스가 등록되지 않았습니다.");
        }
        return service;
    }

    // 모든 PG사 Enum에 대응하는 서비스가 구현됐는지 검증
    private void validateAllProvidersMapped() {
        for (PaymentProvider provider : PaymentProvider.values()) {
            if (!paymentServices.containsKey(provider)) {
                log.warn("[PaymentServiceFactory] {}에 대한 서비스 구현체가 없습니다.", provider);
                throw new IllegalStateException(provider + " 서비스가 구현되지 않았습니다.");
            }
        }
    }
}
