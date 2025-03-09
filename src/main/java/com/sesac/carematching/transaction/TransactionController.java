package com.sesac.carematching.transaction;

import com.sesac.carematching.transaction.dto.TransactionGetDTO;
import com.sesac.carematching.transaction.dto.TransactionAddDTO;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping(value="/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TokenAuth tokenAuth;

    @PostMapping("/add")
    public ResponseEntity<UUID> addTransaction(@RequestBody TransactionAddDTO transactionAddDTO, HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        String caregiverUsername = transactionAddDTO.getReceiverUsername();
        Transaction transaction = transactionService.saveTransaction(username, caregiverUsername);
        return ResponseEntity.ok(transaction.getTransactionId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionGetDTO> getTransactionById(@PathVariable UUID id, HttpServletRequest request) {
        String username = tokenAuth.extractUsernameFromToken(request);
        return ResponseEntity.ok(transactionService.getValidTransaction(id, username));
    }
}
