package com.exe201.project.dto.request.feature;

import jakarta.validation.constraints.NotNull;

public record PurchaseFeatureRequest(
        @NotNull(message = "Membership feature ID is required")
        Long membershipFeatureId
) {
}
