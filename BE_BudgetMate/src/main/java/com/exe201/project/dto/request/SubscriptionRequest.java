package com.exe201.project.dto.request;

import com.exe201.project.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record SubscriptionRequest(
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}
