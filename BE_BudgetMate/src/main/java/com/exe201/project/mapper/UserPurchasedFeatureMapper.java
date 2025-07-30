package com.exe201.project.mapper;

import com.exe201.project.dto.response.user_purchased_feature.UserPurchasedFeatureResponse;
import com.exe201.project.entity.UserPurchasedFeature;
import org.springframework.stereotype.Component;

@Component
public class UserPurchasedFeatureMapper {

    public UserPurchasedFeatureResponse toUserPurchasedFeatureResponse(UserPurchasedFeature purchasedFeature) {
        if (purchasedFeature == null) {
            return null;
        }

        return UserPurchasedFeatureResponse.builder()
                .id(purchasedFeature.getId())
                .featureName(purchasedFeature.getPurchasableFeature().getFeature().getName())
                .featureKey(purchasedFeature.getPurchasableFeature().getFeature().getFeatureKey())
                .remainingUsage(purchasedFeature.getRemainingUsage())
                .lastUsedAt(purchasedFeature.getLastUsedAt())
                .description(purchasedFeature.getPurchasableFeature().getDescription())
                .build();
    }
}
