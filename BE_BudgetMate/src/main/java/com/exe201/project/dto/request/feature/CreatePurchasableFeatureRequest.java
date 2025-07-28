package com.exe201.project.dto.request.feature;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreatePurchasableFeatureRequest(
        @NotNull(message = "Feature ID is required")
        Long featureId,

        @NotNull(message = "Credit price is required")
        @Min(value = 1, message = "Credit price must be at least 1")
        Integer creditPrice,

        @Min(value = 1, message = "Usage limit must be at least 1")
        Integer usageLimit,

        @NotNull(message = "Target membership plans is required")
        List<String> targetMembershipPlans,

        String description
) {}