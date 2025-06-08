package com.exe201.project.dto.request;

import com.exe201.project.entity.Transaction;
import com.exe201.project.entity.User;
import com.exe201.project.enums.WalletType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

public record WalletRequest(

        WalletType type,

        String name,

        @Min(value = 0, message = "Value must be larger than 0")
        double targetAmount,

        @Min(value = 0, message = "Value must be larger than 0")
        double interestRate,

        LocalDate deadline
) {
}
