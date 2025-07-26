package com.sesac.carematching.transaction;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverService;
import com.sesac.carematching.transaction.dto.TransactionGetDTO;
import com.sesac.carematching.transaction.dto.TransactionVerifyDTO;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    // 결제 가능 시간 (분)
    private final static long MAX_PAYMENT_TIME = 30;

    private final TransactionRepository transactionRepository;
    private final CaregiverService caregiverService;
    private final UserService userService;
    private final TossPaymentService tossPaymentService;

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
        // TossPayments 결제 검증 - TossPaymentService 사용
        boolean isValid = tossPaymentService.verifyTossPayment(orderId, price, paymentKey);
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
