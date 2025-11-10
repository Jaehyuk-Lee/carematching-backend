package com.sesac.carematching.transaction;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverService;
import com.sesac.carematching.transaction.dto.PaymentConfirmRequestDTO;
import com.sesac.carematching.transaction.dto.TransactionConfirmDTO;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import com.sesac.carematching.transaction.dto.TransactionGetDTO;
import com.sesac.carematching.transaction.payment.PaymentGatewayRouter;
import com.sesac.carematching.transaction.payment.PaymentProvider;
import com.sesac.carematching.transaction.payment.PaymentService;
import com.sesac.carematching.transaction.payment.PgStatus;
import com.sesac.carematching.transaction.payment.provider.kakao.KakaoPayService;
import com.sesac.carematching.transaction.payment.provider.toss.TossPaymentService;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Service
public class TransactionService {
    // 결제 가능 시간 (분)
    private final static long MAX_PAYMENT_MINUTE = 30;

    private final TransactionRepository transactionRepository;
    private final CaregiverService caregiverService;
    private final UserService userService;
    private final Map<PaymentProvider, PaymentService> paymentServices = new EnumMap<>(PaymentProvider.class);
    private final PaymentGatewayRouter paymentGatewayRouter;

    public TransactionService(TransactionRepository transactionRepository,
                              CaregiverService caregiverService,
                              UserService userService,
                              TossPaymentService tossPaymentService,
                              KakaoPayService kakaoPayService,
                              PaymentGatewayRouter paymentGatewayRouter) {

        this.transactionRepository = transactionRepository;
        this.caregiverService = caregiverService;
        this.userService = userService;
        this.paymentGatewayRouter = paymentGatewayRouter;

        this.paymentServices.put(PaymentProvider.TOSS, tossPaymentService);
        this.paymentServices.put(PaymentProvider.KAKAO, kakaoPayService);
    }

    @Transactional
    public Transaction saveTransaction(String username, String caregiverUsername) {
        Transaction transaction = new Transaction();
        Caregiver caregiver = caregiverService.findByUserId(userService.getUserInfo(caregiverUsername).getId());
        User user = userService.getUserInfo(username);

        transaction.setCno(caregiver);
        transaction.setUno(user);
        transaction.setPrice(caregiver.getSalary());
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transaction.setPaymentProvider(paymentGatewayRouter.getActiveProvider());

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionGetDTO getValidTransaction(String orderId, String username) {
        Transaction transaction = transactionRepository.findByOrderId(orderId).orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다."));

        if (!transaction.getUno().getUsername().equals(username)) {
            throw new IllegalCallerException("해당 결제는 다른 사용자의 결제 요청입니다.");
        }

        if (transaction.getTransactionStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("결제 대기 중인 주문이 아닙니다.");
        }

        // 결제 가능 시간 확인
        long minutesDifference = java.time.Duration.between(
            transaction.getCreatedAt(),
            java.time.Instant.now()
        ).toMinutes();
        if (minutesDifference > MAX_PAYMENT_MINUTE) {
            throw new IllegalStateException("결제 가능 시간이 만료되었습니다. 새로운 결제를 시도해주세요.");
        }

        TransactionGetDTO transactionGetDTO = new TransactionGetDTO();
        transactionGetDTO.setCaregiverName(transaction.getCno().getRealName());
        transactionGetDTO.setUserName(transaction.getUno().getUsername());
        transactionGetDTO.setPrice(transaction.getPrice());
        return transactionGetDTO;
    }

    @Transactional
    public TransactionConfirmDTO confirmTransaction(TransactionConfirmDTO transactionConfirmDTO, Integer userId, String paymentKey) {
        String orderId = transactionConfirmDTO.getOrderId();
        Integer price = transactionConfirmDTO.getPrice();
        Transaction transaction = verifyTransaction(orderId, price, userId);
        PaymentProvider pg = transaction.getPaymentProvider();

        // 현재 결제의 PG사에 알맞는 confirmRequestDTO 생성
        PaymentConfirmRequestDTO request;
        TransactionDetailDTO transactionDetailDTO;
        if (pg == PaymentProvider.TOSS) {
            request = PaymentConfirmRequestDTO.builder()
                .orderId(orderId)
                .amount(price)
                .paymentKey(paymentKey)
                .build();
            transactionDetailDTO = paymentServices.get(pg).confirmPayment(request);
        } else if (pg == PaymentProvider.KAKAO) {
            String pgToken = transactionConfirmDTO.getPgToken();
            if (pgToken == null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "KakaoPay 결제시 pgToken은 필수값입니다. pgToken이 비어 있습니다"
                );
            }
            request = PaymentConfirmRequestDTO.builder()
                .orderId(orderId)
                .amount(price)
                .paymentKey(paymentKey)
                .partnerUserId(userId.toString()) // 카카오: userId 추가 필요
                .pgToken(pgToken) // 카카오: pgToken 추가 필요
                .build();
            transactionDetailDTO = paymentServices.get(pg).confirmPayment(request);
        } else {
            throw new RuntimeException("confirm이 지원되지 않는 PG사: " + pg);
        }

        // DONE: 인증된 결제수단으로 요청한 결제가 승인된 상태입니다. (https://docs.tosspayments.com/reference#payment-%EA%B0%9D%EC%B2%B4)
        // KakaoPay 응답도 승인 성공시 자체적으로 Status를 DONE으로 설정하였음
        if (transactionDetailDTO.getPgStatus() != PgStatus.DONE)
            throw new RuntimeException("결제 승인에 실패했습니다.");

        transaction.setPgPaymentKey(transactionDetailDTO.getPaymentKey());
        transaction.setOrderName(transactionDetailDTO.getOrderName());
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        TransactionConfirmDTO result = new TransactionConfirmDTO();
        result.setOrderId(transaction.getOrderId());
        result.setPrice(transaction.getPrice());
        return result;
    }

    private Transaction verifyTransaction(String orderId, Integer paidPrice, Integer paidUserId) {
        Transaction transaction = transactionRepository.findByOrderId(orderId).orElseThrow(() -> new EntityNotFoundException("Order ID를 찾을 수 없습니다."));
        Integer shouldId = transaction.getUno().getId();
        Integer shouldPrice = transaction.getPrice();
        if (!shouldId.equals(paidUserId)) {
            log.warn("결제한 사용자 ID: {} | 결제 해야하는 사용자 ID: {}", paidUserId, shouldId);
            throw new IllegalCallerException("해당 결제는 다른 사용자의 결제 요청입니다.");
        }
        if (!shouldPrice.equals(paidPrice)) {
            log.warn("사용자가 결제한 금액: {} | 결제 해야하는 금액: {}", paidPrice, shouldPrice);
            throw new IllegalArgumentException("잘못된 가격이 결제되었습니다.");
        }
        if (transaction.getTransactionStatus() == TransactionStatus.SUCCESS ||
            transaction.getTransactionStatus() == TransactionStatus.CANCELED ||
            transaction.getTransactionStatus() == TransactionStatus.REFUNDED) {
            throw new IllegalStateException("이미 한 번 승인된 거래 입니다.");
        }
        return transaction;
    }
}
