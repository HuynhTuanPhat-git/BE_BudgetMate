package com.exe201.project.dto.response.purchasable_feature;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record PurchasableFeatureResponse(
        UUID id,
        String featureName,
        String featureKey,
        Integer creditPrice,
        Integer usageLimit,
        Boolean isActive,
        List<String> targetMembershipPlans,
        String description
) {}