package com.sesac.carematching.transaction;

import com.sesac.carematching.caregiver.Caregiver;
import com.sesac.carematching.caregiver.CaregiverRepository;
import com.sesac.carematching.caregiver.CaregiverService;
import com.sesac.carematching.transaction.dto.TransactionGetDTO;
import com.sesac.carematching.transaction.dto.TransactionMakeDTO;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import com.sesac.carematching.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CaregiverService caregiverService;
    private final UserService userService;

    @Transactional
    public Transaction saveTransaction(TransactionMakeDTO transactionMakeDTO) {
        Transaction transaction = new Transaction();
        Caregiver caregiver = caregiverService.findById(transactionMakeDTO.getCno());
        User user = userService.findById(transactionMakeDTO.getUno());

        transaction.setCno(caregiver);
        transaction.setUno(user);
        transaction.setPrice(caregiver.getSalary());

        return transactionRepository.save(transaction);
    }

    public TransactionGetDTO getTransactionById(UUID id) {
        Transaction transaction = transactionRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Transaction Not Found!"));
        TransactionGetDTO transactionGetDTO = new TransactionGetDTO();
        transactionGetDTO.setCaregiverName(transaction.getCno().getRealName());
        transactionGetDTO.setUserName(transaction.getUno().getUsername());
        transactionGetDTO.setPrice(transaction.getPrice());
        return transactionGetDTO;
    }
}
