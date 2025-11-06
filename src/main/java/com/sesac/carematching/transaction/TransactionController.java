package com.sesac.carematching.transaction;

import com.sesac.carematching.transaction.dto.TransactionGetDTO;
import com.sesac.carematching.transaction.dto.TransactionAddDTO;
import com.sesac.carematching.transaction.dto.TransactionOrderAddDTO;
import com.sesac.carematching.transaction.dto.TransactionVerifyDTO;
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
    public ResponseEntity<String> addTransaction(@RequestBody TransactionAddDTO transactionAddDTO, HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        String caregiverUsername = transactionAddDTO.getReceiverUsername();
        Transaction transaction = transactionService.saveTransaction(username, caregiverUsername);
        return ResponseEntity.ok(transaction.getTransactionId());
    }

    @Operation(summary = "거래 단건 조회", description = "거래 ID로 거래 정보를 조회합니다.")
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionGetDTO> getTransactionById(@PathVariable String transactionId, HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        return ResponseEntity.ok(transactionService.getValidTransaction(transactionId, username));
    }

    @Operation(summary = "결제 성공 처리", description = "결제 성공 시 거래를 완료 처리합니다.")
    @PostMapping("/verify/{paymentKey}")
    public ResponseEntity<TransactionVerifyDTO> tossPaymentVerify(@RequestBody TransactionVerifyDTO transactionVerifyDTO, @PathVariable String paymentKey, HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        String transactionId = transactionVerifyDTO.getTransactionId();
        Integer price = transactionVerifyDTO.getPrice();
        return ResponseEntity.ok().body(transactionService.transactionVerify(transactionId, price, username, paymentKey));
    }
}
