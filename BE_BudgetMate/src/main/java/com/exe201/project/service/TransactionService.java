package com.exe201.project.service;


import com.exe201.project.dto.request.TransactionRequest;
import com.exe201.project.dto.request.TransactionSummary;
import com.exe201.project.dto.response.TransactionResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    TransactionResponse createTransaction(TransactionRequest request);
    List<TransactionResponse> getTransactionsByWallet(Long walletId);
    List<TransactionResponse> getTransactionsByDateRange(Long walletId, LocalDateTime startDate, LocalDateTime endDate);
    TransactionResponse getTransactionById(Long transactionId);
    TransactionResponse updateTransaction(Long transactionId, TransactionRequest request);
    void deleteTransaction(Long transactionId);
    TransactionSummary getTransactionSummary(Long walletId);
    List<TransactionResponse> getDeletedTransactions(Long walletId); // For audit trail
}
