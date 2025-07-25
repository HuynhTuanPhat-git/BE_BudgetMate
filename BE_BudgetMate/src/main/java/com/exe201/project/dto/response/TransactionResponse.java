package com.exe201.project.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TransactionResponse(
        Long id,
        Double amount,
        String description,
        LocalDateTime transactionTime,
        Long walletId,
        String walletName,
        Long categoryId,
        String categoryName,
        boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        // Original values for audit trail (when transaction is updated)
        Double originalAmount,
        String originalDescription,
        LocalDateTime originalTransactionTime
) {
}
