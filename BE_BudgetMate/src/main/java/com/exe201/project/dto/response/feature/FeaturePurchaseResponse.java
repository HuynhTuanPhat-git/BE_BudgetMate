package com.exe201.project.dto.response.feature;

import lombok.Builder;

@Builder
public record FeaturePurchaseResponse(
        String featureName,
        String featureKey,
        Integer creditSpent,
        Integer remainingCredits,
        String message
) {
}
