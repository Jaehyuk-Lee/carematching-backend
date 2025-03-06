package com.sesac.carematching.transaction;

import com.sesac.carematching.transaction.dto.TransactionGetDTO;
import com.sesac.carematching.transaction.dto.TransactionAddDTO;
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

    @PostMapping("/add")
    public ResponseEntity<UUID> get(@RequestBody TransactionAddDTO transactionAddDTO) {
        Transaction transaction = transactionService.saveTransaction(transactionAddDTO);
        return ResponseEntity.ok(transaction.getTransactionId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionGetDTO> getTransactionById(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.getValidTransaction(id));
    }
}
