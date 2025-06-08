package com.exe201.project.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TransactionRequest(
        
        @NotNull(message = "Amount is required")
        @Min(value = 0, message = "Amount must be positive")
        Double amount,
        
        String description,
        
        @NotNull(message = "Wallet ID is required")
        Long walletId,
        
        Long categoryId,
        
        LocalDateTime transactionTime
) {
}
