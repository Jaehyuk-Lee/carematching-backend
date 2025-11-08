package com.sesac.carematching.transaction.payment.provider.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sesac.carematching.transaction.payment.AbstractPaymentService;
import com.sesac.carematching.transaction.payment.PaymentProvider;
import com.sesac.carematching.transaction.payment.PaymentService;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.payment.PgStatus;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import com.sesac.carematching.transaction.payment.client.PaymentClient;
import com.sesac.carematching.transaction.payment.pendingPayment.PendingPayment;
import com.sesac.carematching.transaction.payment.pendingPayment.PendingPaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class KakaoPayService extends AbstractPaymentService {
    private final PaymentClient paymentClient;

    // 카카오페이 API CID
    @Value("${kakao.cid}")
    private String kakao_cid;

    // 카카오페이 API 시크릿키
    @Value("${kakao.secret}")
    private String kakao_secret;

    public KakaoPayService(PendingPaymentRepository pendingPaymentRepository, PaymentClient paymentClient) {
        super(pendingPaymentRepository);
        this.paymentClient = paymentClient;
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

        try {
            ResponseEntity<String> response = paymentClient.send(url, entity);
            return paymentDone(response.getBody(), objectMapper);
        } catch (RestClientResponseException e) { // RestTemplate 응답 상태 코드가 2xx, 3xx 아니면 터짐
            throw parsePaymentError(e.getResponseBodyAsString(), KakaoPayException.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected PaymentProvider getPaymentProvider() {
        return PaymentProvider.KAKAO;
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

    protected void customizePendingPayment(PendingPayment pending, PaymentConfirmRequestDTO request) {
        pending.setPgToken(request.getPgToken());
        pending.setPartnerUserId(request.getPartnerUserId());
    }
}
