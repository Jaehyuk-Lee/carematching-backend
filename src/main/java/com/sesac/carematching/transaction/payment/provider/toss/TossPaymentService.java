package com.sesac.carematching.transaction.payment.provider.toss;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import com.sesac.carematching.transaction.payment.AbstractPaymentService;
import com.sesac.carematching.transaction.payment.PaymentProvider;
import com.sesac.carematching.transaction.payment.PgStatus;
import com.sesac.carematching.transaction.payment.client.PaymentClient;
import com.sesac.carematching.transaction.payment.pendingPayment.PendingPaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.Base64;

@Slf4j
@Service
public class TossPaymentService extends AbstractPaymentService {
    private final PaymentClient paymentClient;

    // 토스 페이먼츠 API 시크릿키
    @Value("${toss.secret}")
    private String tossSecret;

    public TossPaymentService(PendingPaymentRepository pendingPaymentRepository, PaymentClient paymentClient) {
        super(pendingPaymentRepository);
        this.paymentClient = paymentClient;
    }

    @Override
    @CircuitBreaker(name = "TossPayments_Confirm", fallbackMethod = "fallbackForConfirm")
    public TransactionDetailDTO confirmPayment(PaymentConfirmRequestDTO request) {
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

        HttpEntity<String> entity = new HttpEntity<>(requestData.toString(), headers);

        try {
            ResponseEntity<String> response = paymentClient.send(url, entity);
            return paymentDone(response.getBody(), objectMapper);
        } catch (RestClientResponseException e) { // RestTemplate 응답 상태 코드가 2xx, 3xx 아니면 터짐
            throw parsePaymentError(e.getResponseBodyAsString(), TossPaymentsException.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected PaymentProvider getPaymentProvider() {
        return PaymentProvider.TOSS;
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
        transactionDetailDTO.setPgStatus(PgStatus.valueOf(statusNode.asText()));
        transactionDetailDTO.setOrderId(orderIdNode.asText());
        transactionDetailDTO.setOrderName(orderNameNode.asText());
        return transactionDetailDTO;
    }
}
