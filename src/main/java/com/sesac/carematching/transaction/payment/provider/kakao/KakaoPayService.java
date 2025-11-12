package com.sesac.carematching.transaction.payment.provider.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sesac.carematching.transaction.Transaction;
import com.sesac.carematching.transaction.TransactionRepository;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentReadyRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentReadyResponseDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import com.sesac.carematching.transaction.payment.AbstractPaymentService;
import com.sesac.carematching.transaction.payment.PaymentProvider;
import com.sesac.carematching.transaction.payment.PgStatus;
import com.sesac.carematching.transaction.payment.client.PaymentClient;
import com.sesac.carematching.transaction.payment.pendingPayment.PendingPayment;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class KakaoPayService extends AbstractPaymentService {
    private static final String KAKAO_BASE_URL = "https://open-api.kakaopay.com";
    private final PaymentClient paymentClient;

    // 카카오페이 API CID
    @Value("${kakao.cid}")
    private String kakao_cid;

    // 카카오페이 API 시크릿키
    @Value("${kakao.secret}")
    private String kakao_secret;

    // 카카오페이 API 리다이렉트용 프론트엔드 도메인
    @Value("${frontend-domain}")
    private String frontend_domain;

    public KakaoPayService(TransactionRepository transactionRepository, PaymentClient paymentClient) {
        super(transactionRepository);
        this.paymentClient = paymentClient;
    }

    @Override
    public PaymentProvider getPaymentProvider() {
        return PaymentProvider.KAKAO;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "KakaoPay_Ready", fallbackMethod = "fallbackForReady")
    public PaymentReadyResponseDTO readyPayment(PaymentReadyRequestDTO request) {
        // 카카오페이API 준비 문서: https://developers.kakaopay.com/docs/payment/online/single-payment#payment-ready
        String url = KAKAO_BASE_URL + "/online/v1/payment/ready";
        ObjectMapper objectMapper = new ObjectMapper();

        // 요청 데이터 생성
        ObjectNode requestData = objectMapper.createObjectNode();
        requestData.put("cid", kakao_cid);
        requestData.put("partner_order_id", request.getOrderId());
        requestData.put("partner_user_id", request.getUserId());
        requestData.put("item_name", request.getItemName());
        requestData.put("quantity", request.getQuantity());
        requestData.put("total_amount", request.getTotalAmount());
        requestData.put("tax_free_amount", 0); // taxFreeAmount는 필요 없으므로 0으로 설정
        requestData.put("approval_url", frontend_domain + "/payment/kakao-success?orderId=" + request.getOrderId());
        requestData.put("cancel_url", frontend_domain + "/");
        requestData.put("fail_url", frontend_domain + "/payment/kakao-fail?orderId=" + request.getOrderId());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + kakao_secret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity;
        try {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(requestData), headers);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        try {
            ResponseEntity<String> response = paymentClient.send(url, entity);
            JsonNode json = objectMapper.readTree(response.getBody());

            String tid = json.get("tid").asText();
            String nextRedirectPcUrl = json.get("next_redirect_pc_url").asText();
            String createdAtString = json.get("created_at").asText(); // '2025-11-10T16:10:21' 형식을 응답받음
            Instant createdAt = LocalDateTime
                .parse(createdAtString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant();

            // Transaction에 tid(pgPaymentKey) 저장
            Transaction transaction = transactionRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다."));
            transaction.setPgPaymentKey(tid);
            transactionRepository.save(transaction);

            return new PaymentReadyResponseDTO(nextRedirectPcUrl, tid, createdAt);

        } catch (RestClientResponseException e) {
            throw parsePaymentError(e.getResponseBodyAsString(), KakaoPayException.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PaymentReadyResponseDTO fallbackForReady(PaymentReadyRequestDTO request) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "카카오페이 Ready API 서버 장애. 다른 PG사를 이용해주세요.");
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "KakaoPay_Confirm", fallbackMethod = "fallbackForConfirm")
    public TransactionDetailDTO confirmPayment(PaymentConfirmRequestDTO request) {
        // 카카오페이API 승인 문서: https://developers.kakaopay.com/docs/payment/online/single-payment#payment-approve-request
        String url = KAKAO_BASE_URL + "/online/v1/payment/approve";
        ObjectMapper objectMapper = new ObjectMapper();

        // 요청 데이터 생성
        ObjectNode requestData = objectMapper.createObjectNode();
        requestData.put("cid", kakao_cid);
        requestData.put("partner_order_id", request.getOrderId());
        requestData.put("total_amount", request.getAmount());
        requestData.put("tid", request.getPaymentKey());
        requestData.put("partner_user_id", request.getPartnerUserId());
        requestData.put("pg_token", request.getPgToken());

        HttpHeaders headers = new HttpHeaders();
        // KakaoPay 인증 헤더 구조 - Authorization: SECRET_KEY ${SECRET_KEY}
        headers.set("Authorization", "SECRET_KEY " + kakao_secret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity;
        try {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(requestData), headers);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        try {
            ResponseEntity<String> response = paymentClient.send(url, entity);
            return paymentDone(response.getBody(), objectMapper);
        } catch (RestClientResponseException e) { // RestTemplate 응답 상태 코드가 2xx, 3xx 아니면 터짐
            throw parsePaymentError(e.getResponseBodyAsString(), KakaoPayException.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        // 비어있는 경우, 승인되지 않았는데 HTTP 200 메시지가 온 것이 이상하니 UNKNOWN 처리
        if (approvedAtNode.asText().isEmpty()) {
            log.warn("KakaoPay 응답에서 approved_at 필드가 비어있습니다.");
            transactionDetailDTO.setPgStatus(PgStatus.UNKNOWN);
        } else {
            transactionDetailDTO.setPgStatus(PgStatus.DONE);
        }
        return transactionDetailDTO;
    }

    protected void customizePendingPayment(PendingPayment pending, PaymentConfirmRequestDTO request) {
        pending.setPgToken(request.getPgToken());
        pending.setPartnerUserId(request.getPartnerUserId());
    }
}
