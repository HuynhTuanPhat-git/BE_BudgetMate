package com.exe201.project.dto.response;

import com.exe201.project.entity.*;
import com.exe201.project.enums.UserStatus;
import com.exe201.project.enums.WalletType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String address,
        String avatar,
        Integer streakDays,
        LocalDateTime lastLoginDate,
        Integer credits,
        UserStatus status,
        Long roleId,
        String roleName,
        Long petId,
        String petName
) {
}
