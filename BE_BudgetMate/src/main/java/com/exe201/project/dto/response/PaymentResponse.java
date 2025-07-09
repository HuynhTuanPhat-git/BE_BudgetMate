package com.exe201.project.dto.response;

import lombok.Builder;

@Builder
public record PaymentResponse(
        String orderCode,
        String checkoutUrl,
        String qrCode,
        Double amount,
        String currency,
        String status,
        String membershipPlanName,
        Long subscriptionId
) {
}
