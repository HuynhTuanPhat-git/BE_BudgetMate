package com.exe201.project.dto.response;

import com.exe201.project.entity.Transaction;
import com.exe201.project.entity.User;
import com.exe201.project.entity.Wallets;
import com.exe201.project.enums.WalletType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record WalletResponse(
        Long id,

        WalletType type,

        String name,

        double balance,

        double targetAmount,

        double interestRate,

        LocalDate deadline,

        User user,

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
