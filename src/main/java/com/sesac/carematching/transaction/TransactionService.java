package com.sesac.carematching.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverService;
import com.sesac.carematching.transaction.dto.TossPaymentsErrorResponseDTO;
import com.sesac.carematching.transaction.exception.TossPaymentsException;
import com.sesac.carematching.transaction.dto.TransactionGetDTO;
import com.sesac.carematching.transaction.dto.TransactionVerifyDTO;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Base64;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    // 결제 가능 시간 (분)
    private final static long MAX_PAYMENT_TIME = 30;

    private final TransactionRepository transactionRepository;
    private final CaregiverService caregiverService;
    private final UserService userService;

    // 토스 페이먼츠 API 시크릿키
    @Value("${toss.secret}")
    private String tossSecret;

    @Transactional
    public Transaction saveTransaction(String username, String caregiverUsername) {
        Transaction transaction = new Transaction();
        Caregiver caregiver = caregiverService.findByUserId(userService.getUserInfo(caregiverUsername).getId());
        User user = userService.getUserInfo(username);

        transaction.setCno(caregiver);
        transaction.setUno(user);
        transaction.setPrice(caregiver.getSalary());
        transaction.setStatus(Status.PENDING);

        return transactionRepository.save(transaction);
    }

    public TransactionGetDTO getValidTransaction(UUID id, String username) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다."));

        if (!transaction.getUno().getUsername().equals(username)) {
            throw new IllegalCallerException("해당 결제는 다른 사용자의 결제 요청입니다.");
        }

        if (transaction.getStatus() != Status.PENDING) {
            throw new IllegalStateException("결제 대기 중인 주문이 아닙니다.");
        }

        // 결제 가능 시간 확인
        long minutesDifference = java.time.Duration.between(
            transaction.getCreatedAt(),
            java.time.Instant.now()
        ).toMinutes();
        if (minutesDifference > MAX_PAYMENT_TIME) {
            throw new IllegalStateException("결제 가능 시간이 만료되었습니다. 새로운 결제를 시도해주세요.");
        }

        TransactionGetDTO transactionGetDTO = new TransactionGetDTO();
        transactionGetDTO.setCaregiverName(transaction.getCno().getRealName());
        transactionGetDTO.setUserName(transaction.getUno().getUsername());
        transactionGetDTO.setPrice(transaction.getPrice());
        return transactionGetDTO;
    }

    public void saveOrderId(UUID transactionId, String orderId, Integer price, String username) {
        Transaction transaction = verifyTransaction(transactionId, price, username);

        transaction.setOrderId(orderId);
        transactionRepository.save(transaction);
    }

    public TransactionVerifyDTO transactionVerify(String orderId, Integer price, String username, String paymentKey) {
        // TossPayments 결제 검증
        boolean isValid = verifyTossPayment(orderId, price, paymentKey);
        if (!isValid) {
            throw new IllegalStateException("결제 검증에 실패했습니다.");
        }

        UUID transactionId = transactionRepository.findByOrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException("order ID를 찾지 못하였습니다."))
            .getTransactionId();
        Transaction transaction = verifyTransaction(transactionId, price, username);

        transaction.setStatus(Status.SUCCESS);
        transaction.setPaidPrice(price);

        transactionRepository.save(transaction);

        TransactionVerifyDTO result = new TransactionVerifyDTO();
        result.setOrderId(orderId);
        result.setPrice(price);
        return result;
    }

    private boolean verifyTossPayment(String orderId, Integer price, String paymentKey) {
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
            int timeout = baseTimeout + (int)((maxTimeout - baseTimeout) * (attempt - 1) / (maxAttempts - 1));
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(timeout);
            factory.setReadTimeout(timeout);
            RestTemplate restTemplate = new RestTemplate(factory);
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                return isPaymentDone(response.getBody(), objectMapper);
            } catch (RestClientResponseException e) {
                handleTossPaymentsError(e);
            } catch (ResourceAccessException e) {
                handleNetworkError(e, attempt, maxAttempts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("TossPayments 결제 검증 중 알 수 없는 오류 발생");
    }

    private boolean isPaymentDone(String responseBody, ObjectMapper objectMapper) throws JsonProcessingException {
        JsonNode json = objectMapper.readTree(responseBody);
        String status = json.has("status") ? json.get("status").asText() : null;
        return "DONE".equals(status);
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

    private Transaction verifyTransaction(UUID transactionId, Integer price, String username) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new EntityNotFoundException("Transaction ID를 찾을 수 없습니다."));
        if (!transaction.getUno().getUsername().equals(username)) {
            throw new IllegalCallerException("해당 결제는 다른 사용자의 결제 요청입니다.");
        }
        if (!transaction.getPrice().equals(price)) {
            throw new IllegalArgumentException("가격 정보가 잘못되었습니다.");
        }
        return transaction;
    }
}
