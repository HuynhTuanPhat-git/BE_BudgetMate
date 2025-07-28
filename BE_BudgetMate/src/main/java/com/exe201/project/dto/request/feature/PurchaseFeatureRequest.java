package com.exe201.project.dto.request.feature;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PurchaseFeatureRequest(
        @NotNull(message = "Purchasable feature ID is required")
        UUID purchasableFeatureId
) {}
