package com.exe201.project.dto.response.user_purchased_feature;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserPurchasedFeatureResponse(
        UUID id,
        String featureName,
        String featureKey,
        Integer remainingUsage,
        LocalDateTime lastUsedAt,
        String description
) {}
