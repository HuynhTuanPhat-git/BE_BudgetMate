package com.exe201.project.dto.request;

public record TransactionSummary(
        Double totalIncome,
        Double totalExpense,
        Double netAmount,
        Long transactionCount
) {}