package com.exe201.project.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTransactionResponse {
    private LocalDate date;
    private Integer totalTransactions;
    private Integer successfulTransactions;
    private Integer failedTransactions;
    private Double totalAmount;
    private Integer uniqueUsers;
}
