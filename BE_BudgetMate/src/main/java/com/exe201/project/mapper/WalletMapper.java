package com.exe201.project.mapper;

import com.exe201.project.dto.response.WalletResponse;
import com.exe201.project.entity.Category;
import com.exe201.project.entity.Transaction;
import com.exe201.project.entity.Wallets;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class WalletMapper {

    public WalletResponse toWalletResponse(Wallets wallet) {
        if (wallet == null) {
            return null;
        }

        return WalletResponse.builder()
                .id(wallet.getId())
                .type(wallet.getType())
                .name(wallet.getName())
                .balance(wallet.getBalance())
                .targetAmount(wallet.getTargetAmount())
                .interestRate(wallet.getInterestRate())
                .deadline(wallet.getDeadline())
                .transactions(Collections.emptyList()) // Empty list for basic wallet response
                .build();
    }
    public WalletResponse toWalletResponseWithTransactions(Wallets wallet) {
        if (wallet == null) {
            return null;
        }

        List<WalletResponse.Transaction> transactionResponses = mapTransactions(wallet.getTransactions());

        return WalletResponse.builder()
                .id(wallet.getId())
                .type(wallet.getType())
                .name(wallet.getName())
                .balance(wallet.getBalance())
                .targetAmount(wallet.getTargetAmount())
                .interestRate(wallet.getInterestRate())
                .deadline(wallet.getDeadline())
                .transactions(transactionResponses)
                .build();
    }

    private List<WalletResponse.Transaction> mapTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyList();
        }

        return transactions.stream()
                .map(this::mapSingleTransaction)
                .collect(Collectors.toList());
    }

    /**
     * Maps a single Transaction entity to WalletResponse.Transaction DTO
     *
     * @param transaction Transaction entity to map
     * @return Mapped transaction DTO
     */
    private WalletResponse.Transaction mapSingleTransaction(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return WalletResponse.Transaction.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionTime(transaction.getTransactionTime())
                .categoryName(extractCategoryName(transaction))
                .build();
    }

    private String extractCategoryName(Transaction transaction) {
        return Optional.ofNullable(transaction)
                .map(Transaction::getCategory)
                .map(Category::getName)
                .orElse(null);
    }
}
