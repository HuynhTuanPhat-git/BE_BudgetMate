package com.exe201.project.dto.response.credit_transaction;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CreditTransactionResponse(
        UUID id,
        String featureName,
        String featureKey,
        Integer creditSpent,
        LocalDateTime transactionTime,
        String description
) {
}
