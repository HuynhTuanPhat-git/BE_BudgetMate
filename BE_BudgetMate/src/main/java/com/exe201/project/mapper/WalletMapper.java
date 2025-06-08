package com.exe201.project.mapper;

import com.exe201.project.dto.response.WalletResponse;
import com.exe201.project.entity.Wallets;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WalletMapper {

    public WalletResponse toWalletResponse(Wallets wallet) {
        List<WalletResponse.Transaction> transactionResponses = wallet.getTransactions() != null ?
                wallet.getTransactions().stream()
                        .map(transaction -> WalletResponse.Transaction.builder()
                                .id(transaction.getId())
                                .amount(transaction.getAmount())
                                .description(transaction.getDescription())
                                .transactionTime(transaction.getTransactionTime())
                                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                                .build())
                        .collect(Collectors.toList()) : List.of();

        return WalletResponse.builder()
                .id(wallet.getId())
                .type(wallet.getType())
                .name(wallet.getName())
                .balance(wallet.getBalance())
                .targetAmount(wallet.getTargetAmount())
                .interestRate(wallet.getInterestRate())
                .deadline(wallet.getDeadline())
                .user(wallet.getUser())
                .transactions(transactionResponses)
                .build();
    }
}
