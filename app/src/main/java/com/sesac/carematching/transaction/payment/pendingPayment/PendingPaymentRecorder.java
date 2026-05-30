package com.sesac.carematching.transaction.payment.pendingPayment;

import com.sesac.carematching.transaction.Transaction;
import com.sesac.carematching.transaction.TransactionRepository;
import com.sesac.carematching.transaction.TransactionStatus;
import com.sesac.carematching.transaction.payment.PaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

/**
 * 결제 승인 fallback 시 거래를 PENDING_RETRY 상태로 전환하고 PendingPayment를 기록하는 컴포넌트.
 *
 * <p>반드시 {@link Propagation#REQUIRES_NEW}로 별도의 물리 트랜잭션에서 커밋되어야 합니다.
 * fallback은 외부 결제 호출을 감싸는 상위 트랜잭션(TransactionService.confirmTransaction) 안에서
 * 실행되며, 이후 {@code FallbackCheckAspect}가 던지는 {@code ApiFallbackException}으로 인해
 * 상위 트랜잭션은 롤백됩니다. 같은 트랜잭션에 기록하면 재시도 예약 자체가 롤백되어 사라지므로,
 * 여기서 새 트랜잭션으로 분리해 독립적으로 커밋합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class PendingPaymentRecorder {

    private final TransactionRepository transactionRepository;

    /**
     * 거래를 PENDING_RETRY로 전환하고 PendingPayment를 저장합니다. (상위 트랜잭션과 독립적으로 커밋)
     *
     * @param orderId     대상 주문 ID
     * @param provider    재시도에 사용할 PG사
     * @param paymentKey  PG사에서 발급한 결제 키
     * @param customizer  PG사별 PendingPayment 추가 필드 설정 훅 (예: KakaoPay의 pgToken)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String orderId, PaymentProvider provider, String paymentKey,
                       Consumer<PendingPayment> customizer) {
        Transaction transaction = transactionRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalStateException(
                "Fallback: 존재하지 않는 주문 ID에 대한 승인 요청입니다. orderId=" + orderId));

        transaction.changeTransactionStatus(TransactionStatus.PENDING_RETRY);
        transaction.setPaymentProvider(provider);
        transaction.setPgPaymentKey(paymentKey);

        PendingPayment pending = new PendingPayment();
        customizer.accept(pending);

        transaction.setPendingPayment(pending);
        transactionRepository.save(transaction);
    }
}
