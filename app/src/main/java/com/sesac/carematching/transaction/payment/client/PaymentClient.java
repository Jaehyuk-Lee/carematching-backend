package com.sesac.carematching.transaction.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PaymentClient {
    private final List<RestTemplate> restTemplateList;
    private final int maxAttempts;

    public PaymentClient() {
        // 최대 시도 수 설정
        // 사용자 경험을 고려했을 때는 5초가 적당하지만,
        // 모든 경우를 커버하기 위해서는 30초를 권장한다.
        // 토스페이먼츠 개발자 센터 내용: https://techchat.tosspayments.com/m/1261254382864039996
        int[] timeouts = {5000, 17500, 30000};
        this.maxAttempts = timeouts.length;

        // restTemplate 리스트 초기화
        List<RestTemplate> list = new ArrayList<>();
        for (int timeout : timeouts) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(timeout);
            factory.setReadTimeout(timeout);
            list.add(new RestTemplate(factory));
        }
        this.restTemplateList = list;
    }

    public ResponseEntity<String> send(String url, HttpEntity<?> entity) throws RestClientResponseException {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            int index = Math.max(0, Math.min(attempt - 1, restTemplateList.size() - 1));
            RestTemplate restTemplate = restTemplateList.get(index);
            try {
                return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            } catch (ResourceAccessException e) {
                handleNetworkError(e, url, attempt, maxAttempts);
            }
        }
        throw new RuntimeException("결제 검증 중 알 수 없는 오류 발생");
    }

    private void handleNetworkError(ResourceAccessException e, String url, int attempt, int maxAttempts) {
        log.warn("{}로의 Payment API 요청 네트워크 오류 발생 ({}회차): {}", url, attempt, e.getMessage());
        if (attempt == maxAttempts) {
            throw new RuntimeException("Payment API 요청 네트워크 오류: 최대 재시도 횟수 초과", e);
        }
        try {
            Thread.sleep(1000L * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
