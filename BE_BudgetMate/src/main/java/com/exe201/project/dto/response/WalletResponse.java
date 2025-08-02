package com.exe201.project.dto.response;

import com.exe201.project.entity.Transaction;
import com.exe201.project.enums.WalletStatus;
import com.exe201.project.enums.WalletType;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record WalletResponse(
        Long id,

        WalletType type,

        WalletStatus status,

        String name,

        double balance,

        double targetAmount,

        double interestRate,

        LocalDate deadline,

        // Các field mới cho SAVINGS wallet
        LocalDate startDate,

        Integer termMonths,

        List<Transaction> transactions
) {
    @Builder
    public record Transaction(
            Long id,

            Double amount,

            String description,

            LocalDateTime transactionTime,

            String categoryName
    ) {

    }
}
