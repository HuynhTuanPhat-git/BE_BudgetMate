package com.exe201.project.dto.request;

import com.exe201.project.enums.WalletType;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public record WalletRequest(

        WalletType type,

        String name,

        @Min(value = 0, message = "Value must be larger than 0")
        double targetAmount,

        @Min(value = 0, message = "Value must be larger than 0")
        double interestRate,

        LocalDate deadline,

        // Các field mới cho SAVINGS wallet
        LocalDate startDate,        // Ngày bắt đầu gửi tiết kiệm

        @Min(value = 1, message = "Term must be at least 1 month")
        Integer termMonths          // Kì hạn tính bằng tháng
) {
}
