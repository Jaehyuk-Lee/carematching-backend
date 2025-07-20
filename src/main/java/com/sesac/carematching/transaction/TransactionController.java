package com.sesac.carematching.transaction;

import com.sesac.carematching.transaction.dto.TransactionGetDTO;
import com.sesac.carematching.transaction.dto.TransactionAddDTO;
import com.sesac.carematching.transaction.dto.TransactionOrderAddDTO;
import com.sesac.carematching.transaction.dto.TransactionSuccessDTO;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
    public ResponseEntity<UUID> addTransaction(@RequestBody TransactionAddDTO transactionAddDTO, HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        String caregiverUsername = transactionAddDTO.getReceiverUsername();
        Transaction transaction = transactionService.saveTransaction(username, caregiverUsername);
        return ResponseEntity.ok(transaction.getTransactionId());
    }

    @Operation(summary = "거래 단건 조회", description = "거래 ID로 거래 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionGetDTO> getTransactionById(@PathVariable UUID id, HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        return ResponseEntity.ok(transactionService.getValidTransaction(id, username));
    }

    @Operation(summary = "주문번호 저장", description = "거래에 주문번호와 결제 금액을 저장합니다.")
    @PostMapping("/save-orderid")
    public ResponseEntity<?> saveOrderId(@RequestBody TransactionOrderAddDTO transactionOrderAddDTO, HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        UUID transactionId = transactionOrderAddDTO.getTransactionId();
        String orderId = transactionOrderAddDTO.getOrderId();
        Integer price = transactionOrderAddDTO.getPrice();
        transactionService.saveOrderId(transactionId, orderId, price, username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "결제 성공 처리", description = "결제 성공 시 거래를 완료 처리합니다.")
    @PostMapping("/success")
    public ResponseEntity<TransactionSuccessDTO> tossPaymentSuccess(@RequestBody TransactionSuccessDTO transactionSuccessDTO, HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        String orderId = transactionSuccessDTO.getOrderId();
        Integer price = transactionSuccessDTO.getPrice();
        return ResponseEntity.ok().body(transactionService.transactionSuccess(orderId, price, username));
    }
}
