package com.sesac.carematching.transaction.tosspayments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sesac.carematching.transaction.PaymentProvider;
import com.sesac.carematching.transaction.PaymentService;
import com.sesac.carematching.transaction.dto.TossPaymentsErrorResponseDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import com.sesac.carematching.transaction.pendingPayment.PendingPayment;
import com.sesac.carematching.transaction.pendingPayment.PendingPaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentService implements PaymentService {
    private final PendingPaymentRepository pendingPaymentRepository;
    private final List<RestTemplate> restTemplateList = createRestTemplateList();

    private List<RestTemplate> createRestTemplateList() {
        int[] timeouts = {5000, 17500, 30000};
        List<RestTemplate> list = new ArrayList<>();
        for (int timeout : timeouts) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(timeout);
            factory.setReadTimeout(timeout);
            list.add(new RestTemplate(factory));
        }
        return list;
    }

    // 토스 페이먼츠 API 시크릿키
    @Value("${toss.secret}")
    private String tossSecret;

    @Override
    @CircuitBreaker(name = "TossPayments_Confirm", fallbackMethod = "fallbackForConfirm")
    public TransactionDetailDTO confirmPayment(String orderId, Integer price, String paymentKey) {
        String url = "https://api.tosspayments.com/v1/payments/confirm";
        ObjectMapper objectMapper = new ObjectMapper();

        // 요청 데이터 생성
        ObjectNode requestData = objectMapper.createObjectNode();
        requestData.put("orderId", orderId);
        requestData.put("amount", price);
        requestData.put("paymentKey", paymentKey);

        HttpHeaders headers = new HttpHeaders();
        // Toss Payments는 Basic 인증 사용 - "username:password" 형식을 사용함 (password는 필요없어서 맨 뒤에 콜론만 추가)
        String encodedAuth = Base64.getEncoder().encodeToString((tossSecret + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestData.toString(), headers);

        int maxAttempts = 3;
        // 사용자 경험을 고려했을 때는 5초가 적당하지만,
        // 모든 경우를 커버하기 위해서는 30초를 권장한다.
        // 토스페이먼츠 개발자 센터 내용: https://techchat.tosspayments.com/m/1261254382864039996
        int baseTimeout = 5000; // 5초 - UX 기준
        int maxTimeout = 30000; // 30초 - 모든 경우 커버
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            int index = Math.max(0, Math.min(attempt - 1, restTemplateList.size() - 1));
            RestTemplate restTemplate = restTemplateList.get(index);
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                return paymentDone(response.getBody(), objectMapper);
            } catch (RestClientResponseException e) { // RestTemplate 응답 상태 코드가 4xx, 5xx이면 터짐
                handleTossPaymentsError(e);
            } catch (ResourceAccessException e) {
                handleNetworkError(e, attempt, maxAttempts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // 결제 정보 임시 저장
        fallbackForConfirm(orderId, price, paymentKey, new RuntimeException("3회 재시도 실패"));
        throw new RuntimeException("TossPayments 결제 검증 중 알 수 없는 오류 발생");
    }

    private TransactionDetailDTO paymentDone(String responseBody, ObjectMapper objectMapper) throws JsonProcessingException {
        JsonNode json = objectMapper.readTree(responseBody);
        JsonNode paymentKeyNode = json.get("paymentKey");
        JsonNode statusNode = json.get("status");
        JsonNode orderIdNode = json.get("orderId");
        JsonNode orderNameNode = json.get("orderName");

        if (paymentKeyNode == null || statusNode == null || orderIdNode == null || orderNameNode == null) {
            throw new TossPaymentsException("INVALID_RESPONSE", "TossPayments 응답에 필수 필드가 누락되었습니다");
        }

        TransactionDetailDTO transactionDetailDTO = new TransactionDetailDTO();
        transactionDetailDTO.setPaymentProvider(PaymentProvider.TOSS);
        transactionDetailDTO.setPaymentKey(paymentKeyNode.asText());
        transactionDetailDTO.setStatus(statusNode.asText());
        transactionDetailDTO.setOrderId(orderIdNode.asText());
        transactionDetailDTO.setOrderName(orderNameNode.asText());
        return transactionDetailDTO;
    }

    private void handleTossPaymentsError(RestClientResponseException e) {
        TossPaymentsErrorResponseDTO errorResponse = parseTossPaymentsError(e.getResponseBodyAsString());
        if (errorResponse != null) {
            throw new TossPaymentsException(errorResponse.getCode(), errorResponse.getMessage());
        }
        throw new TossPaymentsException("UNKNOWN_ERROR", "TossPayments 결제 검증 중 알 수 없는 오류 발생");
    }

    private TossPaymentsErrorResponseDTO parseTossPaymentsError(String errorJson) {
        try {
            return new ObjectMapper().readValue(errorJson, TossPaymentsErrorResponseDTO.class);
        } catch (Exception ex) {
            log.warn("TossPayments 에러 메시지 파싱 실패: {}", errorJson);
            return null;
        }
    }

    private void handleNetworkError(ResourceAccessException e, int attempt, int maxAttempts) {
        log.warn("TossPayments 네트워크 오류 발생 ({}회차): {}", attempt, e.getMessage());
        if (attempt == maxAttempts) {
            throw new RuntimeException("TossPayments 네트워크 오류: 최대 재시도 횟수 초과", e);
        }
        try {
            Thread.sleep(1000L * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    // fallbackMethod는 서킷 브레이커가 open일 때 호출됨
    private TransactionDetailDTO fallbackForConfirm(String orderId, Integer price, String paymentKey, Throwable t) {
        // 결제 정보를 PendingPayment에 저장하는 로직 추가
        PendingPayment pending = new PendingPayment(orderId, paymentKey, price, PaymentProvider.TOSS);
        pendingPaymentRepository.save(pending);
        log.warn("TossPayments confirm fallback: 결제 임시 저장. orderId={}, reason={}", orderId, t.getMessage());
        throw new RuntimeException("TossPayments 결제 검증 실패: PendingPayment에 보관", t);
    }
}
