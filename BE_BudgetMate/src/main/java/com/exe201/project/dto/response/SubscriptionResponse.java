package com.exe201.project.dto.response;

import com.exe201.project.enums.PaymentMethod;
import com.exe201.project.enums.PaymentStatus;
import com.exe201.project.enums.SubscriptionStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SubscriptionResponse(
        Long id,
        LocalDate startDate,
        LocalDate endDate,
        SubscriptionStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        MembershipResponse membershipPlan,
        Long userId,
        String userEmail
) {
}
