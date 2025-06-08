package com.exe201.project.controller;

import com.exe201.project.dto.request.TransactionRequest;
import com.exe201.project.dto.request.TransactionSummary;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.TransactionResponse;
import com.exe201.project.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse transaction = transactionService.createTransaction(request);
        return ResponseEntity.ok(
                ApiResponse.<TransactionResponse>builder()
                        .message("Transaction created successfully.")
                        .data(transaction)
                        .build()
        );
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByWallet(@PathVariable Long walletId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByWallet(walletId);
        return ResponseEntity.ok(
                ApiResponse.<List<TransactionResponse>>builder()
                        .message("Transactions retrieved successfully.")
                        .data(transactions)
                        .build()
        );
    }

    @GetMapping("/wallet/{walletId}/date-range")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByDateRange(
            @PathVariable Long walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByDateRange(walletId, startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.<List<TransactionResponse>>builder()
                        .message("Transactions for date range retrieved successfully.")
                        .data(transactions)
                        .build()
        );
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(@PathVariable Long transactionId) {
        TransactionResponse transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(
                ApiResponse.<TransactionResponse>builder()
                        .message("Transaction retrieved successfully.")
                        .data(transaction)
                        .build()
        );
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long transactionId, 
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse transaction = transactionService.updateTransaction(transactionId, request);
        return ResponseEntity.ok(
                ApiResponse.<TransactionResponse>builder()
                        .message("Transaction updated successfully.")
                        .data(transaction)
                        .build()
        );
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Long transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Transaction deleted successfully.")
                        .build()
        );
    }

    @GetMapping("/wallet/{walletId}/summary")
    public ResponseEntity<ApiResponse<TransactionSummary>> getTransactionSummary(@PathVariable Long walletId) {
        TransactionSummary summary = transactionService.getTransactionSummary(walletId);
        return ResponseEntity.ok(
                ApiResponse.<TransactionSummary>builder()
                        .message("Transaction summary retrieved successfully.")
                        .data(summary)
                        .build()
        );
    }


}
