package com.exe201.project.mapper;

import com.exe201.project.dto.response.TransactionResponse;
import com.exe201.project.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionTime(transaction.getTransactionTime())
                .walletId(transaction.getWallet().getId())
                .walletName(transaction.getWallet().getName())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .build();
    }
}
