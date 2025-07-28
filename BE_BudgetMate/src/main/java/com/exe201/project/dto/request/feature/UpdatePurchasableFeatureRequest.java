package com.exe201.project.dto.request.feature;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record UpdatePurchasableFeatureRequest(
        Long featureId,

        @Positive(message = "Credit price must be positive")
        Integer creditPrice,

        @Min(value = 1, message = "Usage limit must be at least 1")
        Integer usageLimit,

        String description,

        List<String> targetMembershipPlans
) {}
