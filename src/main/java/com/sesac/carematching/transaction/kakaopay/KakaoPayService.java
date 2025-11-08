package com.sesac.carematching.transaction.kakaopay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sesac.carematching.transaction.enums.PaymentProvider;
import com.sesac.carematching.transaction.PaymentService;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.enums.PgStatus;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayService implements PaymentService {
    private final PendingPaymentRepository pendingPaymentRepository;
    private final List<RestTemplate> restTemplateList = createRestTemplateList();

    // 카카오페이 API CID
    @Value("${kakao.cid}")
    private String kakao_cid;

    // 카카오페이 API 시크릿키
    @Value("${kakao.secret}")
    private String kakao_secret;

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

    @Override
    @CircuitBreaker(name = "KakaoPay_Confirm", fallbackMethod = "fallbackForConfirm")
    public TransactionDetailDTO confirmPayment(PaymentConfirmRequestDTO request) {
        // 카카오페이API 승인 문서: https://developers.kakaopay.com/docs/payment/online/single-payment#payment-approve-request
        String url = "https://open-api.kakaopay.com/online/v1/payment/approve";
        ObjectMapper objectMapper = new ObjectMapper();

        // 요청 데이터 생성
        ObjectNode requestData = objectMapper.createObjectNode();
        requestData.put("cid", kakao_cid);
        requestData.put("partner_order_id", request.getOrderId());
        requestData.put("total_amount", request.getAmount());
        requestData.put("tid", request.getPaymentKey());
        requestData.put("pg_Token", request.getPgToken());

        HttpHeaders headers = new HttpHeaders();
        // KakaoPay 인증 헤더 구조 - Authorization: SECRET_KEY ${SECRET_KEY}
        headers.set("Authorization", "SECRET_KEY " + kakao_secret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestData.toString(), headers);

        int maxAttempts = 3;
        // 카카오페이에서는 특별히 권장하는 timeout이 없어, 토스페이먼츠 API의 timeout을 그대로 사용함
        // 3회 시도 중: 5초 / 17.5초 / 30초
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            int index = Math.max(0, Math.min(attempt - 1, restTemplateList.size() - 1));
            RestTemplate restTemplate = restTemplateList.get(index);
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                return paymentDone(response.getBody(), objectMapper);
            } catch (RestClientResponseException e) { // RestTemplate 응답 상태 코드가 4xx, 5xx이면 터짐
                handleKakaoPayError(e);
            } catch (ResourceAccessException e) {
                handleNetworkError(e, attempt, maxAttempts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("KakaoPay 결제 검증 중 알 수 없는 오류 발생");
    }

    private TransactionDetailDTO paymentDone(String responseBody, ObjectMapper objectMapper) throws JsonProcessingException {
        JsonNode json = objectMapper.readTree(responseBody);
        // 변수명은 가능하면 TossPayments API 기준으로 작성함
        // 각 PG사에 대응하는 항목 이름은 docs/PG/values.md 참고
        JsonNode paymentKeyNode = json.get("tid");
        JsonNode approvedAtNode = json.get("approved_at");
        JsonNode orderIdNode = json.get("partner_order_id");
        JsonNode orderNameNode = json.get("item_name");

        if (paymentKeyNode == null || approvedAtNode == null || orderIdNode == null || orderNameNode == null) {
            throw new RuntimeException("KakaoPay 응답에 필수 필드가 누락되었습니다");
        }

        TransactionDetailDTO transactionDetailDTO = new TransactionDetailDTO();
        transactionDetailDTO.setPaymentProvider(PaymentProvider.KAKAO);
        transactionDetailDTO.setPaymentKey(paymentKeyNode.asText());
        transactionDetailDTO.setOrderId(orderIdNode.asText());
        transactionDetailDTO.setOrderName(orderNameNode.asText());
        // KakaoPay는 TossPayments API와 달리 Status를 직접 전달해주지 않음.
        // approved_at 필드가 있으면 승인된 것으로 간주
        // NULL인 경우, 승인되지 않았는데 HTTP 200 메시지가 온 것이 이상하니 UNKNOWN 처리
        if (approvedAtNode.isNull()) {
            transactionDetailDTO.setPgStatus(PgStatus.UNKNOWN);
        } else {
            transactionDetailDTO.setPgStatus(PgStatus.DONE);
        }
        return transactionDetailDTO;
    }

    private void handleKakaoPayError(RestClientResponseException e) {
        KakaoPayException errorResponse = parseKakaoPayError(e.getResponseBodyAsString());
        if (errorResponse != null) {
            throw new KakaoPayException(errorResponse.getCode(), errorResponse.getMessage());
        }
        throw new KakaoPayException("UNKNOWN_ERROR", "KakaoPay 결제 검증 중 알 수 없는 오류 발생");
    }

    private KakaoPayException parseKakaoPayError(String errorJson) {
        try {
            return new ObjectMapper().readValue(errorJson, KakaoPayException.class);
        } catch (Exception ex) {
            log.warn("KakaoPay 에러 메시지 파싱 실패: {}", errorJson);
            return null;
        }
    }

    private void handleNetworkError(ResourceAccessException e, int attempt, int maxAttempts) {
        log.warn("KakaoPay 네트워크 오류 발생 ({}회차): {}", attempt, e.getMessage());
        if (attempt == maxAttempts) {
            throw new RuntimeException("KakaoPay 네트워크 오류: 최대 재시도 횟수 초과", e);
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
        PendingPayment pending = new PendingPayment(orderId, paymentKey, price, PaymentProvider.KAKAO);
        pendingPaymentRepository.save(pending);
        log.warn("KakaoPay confirm fallback: 결제 임시 저장. orderId={}, reason={}", orderId, t.getMessage());
        throw new RuntimeException("KakaoPay 결제 승인 실패: PendingPayment에 보관", t);
    }
}
