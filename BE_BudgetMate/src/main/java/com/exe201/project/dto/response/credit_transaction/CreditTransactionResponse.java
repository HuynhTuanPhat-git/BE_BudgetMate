package com.exe201.project.dto.response.credit_transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CreditTransactionResponse(
        UUID id,
        String featureName,
        String featureKey,
        Integer creditSpent,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime transactionTime,
        String description
) {
}
