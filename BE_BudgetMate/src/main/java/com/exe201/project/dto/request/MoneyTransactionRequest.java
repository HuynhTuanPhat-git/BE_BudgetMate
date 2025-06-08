package com.exe201.project.dto.request;

public record MoneyTransactionRequest(
        Double amount,
        String description
) {}