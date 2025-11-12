package com.sesac.carematching.transaction.payment.provider.toss;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sesac.carematching.transaction.TransactionRepository;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentReadyRequestDTO;
import com.sesac.carematching.transaction.dto.PaymentReadyResponseDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import com.sesac.carematching.transaction.payment.AbstractPaymentService;
import com.sesac.carematching.transaction.payment.PaymentProvider;
import com.sesac.carematching.transaction.payment.PgStatus;
import com.sesac.carematching.transaction.payment.client.PaymentClient;
import com.sesac.carematching.util.fallback.FallbackMessage;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

import java.util.Base64;

@Slf4j
@Service
public class TossPaymentService extends AbstractPaymentService {
    private final PaymentClient paymentClient;

    // 토스 페이먼츠 API 시크릿키
    @Value("${toss.secret}")
    private String tossSecret;

    public TossPaymentService(TransactionRepository transactionRepository, PaymentClient paymentClient) {
        super(transactionRepository);
        this.paymentClient = paymentClient;
    }

    @Override
    public PaymentProvider getPaymentProvider() {
        return PaymentProvider.TOSS;
    }

    @Override
    public PaymentReadyResponseDTO readyPayment(PaymentReadyRequestDTO request) {
        throw new UnsupportedOperationException("Payment Ready is not supported on Toss Payments");
    }

    @Override
    @Transactional
    @FallbackMessage(code=202, message="현재 토스페이먼츠 장애 발생으로 결제 승인 처리가 지연되고 있습니다. 10분 내로 결제 처리가 진행됩니다. 이 페이지를 벗어나셔도 괜찮습니다.")
    @CircuitBreaker(name = "TossPayments_Confirm", fallbackMethod = "fallbackForConfirm")
    public TransactionDetailDTO confirmPayment(PaymentConfirmRequestDTO request) {
        // 토스페미언츠 API 승인 문서: https://docs.tosspayments.com/reference#%EA%B2%B0%EC%A0%9C-%EC%8A%B9%EC%9D%B8
        String url = "https://api.tosspayments.com/v1/payments/confirm";
        ObjectMapper objectMapper = new ObjectMapper();

        // 요청 데이터 생성
        ObjectNode requestData = objectMapper.createObjectNode();
        requestData.put("orderId", request.getOrderId());
        requestData.put("amount", request.getAmount());
        requestData.put("paymentKey", request.getPaymentKey());

        HttpHeaders headers = new HttpHeaders();
        // Toss Payments는 Basic 인증 사용 - "username:password" 형식을 사용함 (password는 필요없어서 맨 뒤에 콜론만 추가)
        String encodedAuth = Base64.getEncoder().encodeToString((tossSecret + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
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
            throw parsePaymentError(e.getResponseBodyAsString(), TossPaymentsException.class);
        } catch (JsonProcessingException e) {
            log.error("API 서버에서 응답한 JSON 객체를 파싱하지 못했습니다.");
            throw new RuntimeException("API 서버에서 응답한 JSON 객체를 파싱하지 못했습니다.");
        }
    }

    private TransactionDetailDTO paymentDone(String responseBody, ObjectMapper objectMapper) throws JsonProcessingException {
        // 토스페미언츠 API 응답 문서: https://docs.tosspayments.com/reference#payment-%EA%B0%9D%EC%B2%B4
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
        try {
            transactionDetailDTO.setPgStatus(PgStatus.valueOf(statusNode.asText()));
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 TossPayments 상태값: {}", statusNode.asText());
            transactionDetailDTO.setPgStatus(PgStatus.UNKNOWN);
        }
        transactionDetailDTO.setOrderId(orderIdNode.asText());
        transactionDetailDTO.setOrderName(orderNameNode.asText());
        return transactionDetailDTO;
    }
}
