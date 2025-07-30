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
public class DailyRevenueResponse {
    private LocalDate date;
    private Double revenue;
    private Integer transactionCount;
    private Integer userCount;
}
