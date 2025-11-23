package com.sesac.carematching.transaction;

import com.sesac.carematching.transaction.dto.PaymentReadyResponseDTO;
import com.sesac.carematching.transaction.dto.TransactionAddDTO;
import com.sesac.carematching.transaction.dto.TransactionConfirmDTO;
import com.sesac.carematching.transaction.dto.TransactionGetDTO;
import com.sesac.carematching.util.TokenAuth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@Tag(name = "Transaction Controller", description = "결제 및 거래 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping(value="/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TokenAuth tokenAuth;

    @Operation(summary = "거래 생성", description = "돌봄이와 회원 간의 거래를 생성합니다.")
    @PostMapping("/add")
    public ResponseEntity<String> addTransaction(@RequestBody TransactionAddDTO transactionAddDTO, HttpServletRequest request) {
        Integer userId = tokenAuth.extractUserIdFromToken(request);
        String caregiverUsername = transactionAddDTO.getReceiverUsername();
        Transaction transaction = transactionService.makeTransaction(userId, caregiverUsername);
        return ResponseEntity.ok(transaction.getOrderId());
    }

    @Operation(summary = "거래 단건 조회", description = "거래 ID로 거래 정보를 조회합니다.")
    @GetMapping("/{orderId}")
    public ResponseEntity<TransactionGetDTO> getTransactionById(@PathVariable String orderId, HttpServletRequest request) {
        Integer userId = tokenAuth.extractUserIdFromToken(request);
        return ResponseEntity.ok(transactionService.getTransaction(orderId, userId));
    }

    @Operation(summary = "카카오페이 결제 준비", description = "카카오페이 결제를 준비하고, 리다이렉트 URL을 반환합니다.")
    @PostMapping("/kakao/ready/{orderId}")
    public ResponseEntity<PaymentReadyResponseDTO> readyKakaoPay(@PathVariable String orderId, HttpServletRequest request) {
        Integer userId = tokenAuth.extractUserIdFromToken(request);
        PaymentReadyResponseDTO responseDTO = transactionService.readyPayment(orderId, userId);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "PG사 선택", description = "거래에 사용할 PG사를 선택해줍니다.")
    @PostMapping("/select-pg/{orderId}")
    public ResponseEntity<?> selectPg(@PathVariable String orderId, HttpServletRequest request) {
        Integer userId = tokenAuth.extractUserIdFromToken(request);
        return ResponseEntity.ok().body(transactionService.selectPg(orderId, userId));
    }

    @Operation(summary = "paymentKey(tid) 조회", description = "orderId를 기반으로 paymentKey(tid)를 응답합니다.")
    @GetMapping("/kakao/getPaymentKey/{orderId}")
    public ResponseEntity<String> getPaymentKeyForKakao(@PathVariable String orderId, HttpServletRequest request) {
        Integer userId = tokenAuth.extractUserIdFromToken(request);
        return ResponseEntity.ok().body(transactionService.getPaymentKeyForKakao(orderId, userId));
    }

    @Operation(summary = "결제 성공 처리", description = "결제 성공 시 거래를 완료 처리합니다.")
    @PostMapping("/verify/{paymentKey}")
    public ResponseEntity<TransactionConfirmDTO> confirmPayment(@RequestBody TransactionConfirmDTO transactionConfirmDTO, @PathVariable String paymentKey, HttpServletRequest request) {
        Integer userId = tokenAuth.extractUserIdFromToken(request);
        return ResponseEntity.ok().body(transactionService.confirmTransaction(transactionConfirmDTO, userId, paymentKey));
    }
}
