package com.sesac.carematching.transaction;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverService;
import com.sesac.carematching.transaction.dto.TransactionDetailDTO;
import com.sesac.carematching.transaction.dto.TransactionGetDTO;
import com.sesac.carematching.transaction.dto.TransactionVerifyDTO;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    // 결제 가능 시간 (분)
    private final static long MAX_PAYMENT_MINUTE = 30;

    private final TransactionRepository transactionRepository;
    private final CaregiverService caregiverService;
    private final UserService userService;
    private final PaymentService paymentService;

    @Transactional
    public Transaction saveTransaction(String username, String caregiverUsername) {
        Transaction transaction = new Transaction();
        Caregiver caregiver = caregiverService.findByUserId(userService.getUserInfo(caregiverUsername).getId());
        User user = userService.getUserInfo(username);

        transaction.setCno(caregiver);
        transaction.setUno(user);
        transaction.setPrice(caregiver.getSalary());
        transaction.setStatus(Status.PENDING);
        transaction.setPaymentProvider(PaymentProvider.TOSS);

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionGetDTO getValidTransaction(String orderId, String username) {
        Transaction transaction = transactionRepository.findByOrderId(orderId).orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다."));

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
    public TransactionVerifyDTO transactionVerify(String orderId, Integer price, Integer userId, String paymentKey) {
        Transaction transaction = confirmTransaction(orderId, price, userId);

        // 결제 검증 - 추상화된 PaymentService 사용
        TransactionDetailDTO transactionDetailDTO = paymentService.confirmPayment(orderId, price, paymentKey);
        // DONE: 인증된 결제수단으로 요청한 결제가 승인된 상태입니다. (https://docs.tosspayments.com/reference#payment-%EA%B0%9D%EC%B2%B4)
        if (!"DONE".equals(transactionDetailDTO.getStatus()))
            throw new RuntimeException("결제 승인에 실패했습니다.");

        transaction.setPgPaymentKey(paymentKey);
        transaction.setOrderName(transactionDetailDTO.getOrderName());
        transaction.setStatus(Status.SUCCESS);
        transactionRepository.save(transaction);

        TransactionVerifyDTO result = new TransactionVerifyDTO();
        result.setOrderId(orderId);
        result.setPrice(price);
        return result;
    }

    private Transaction confirmTransaction(String orderId, Integer paidPrice, Integer paidUserId) {
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
        return transaction;
    }
}
