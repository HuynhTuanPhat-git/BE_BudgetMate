package com.exe201.project.service;

import com.exe201.project.dto.response.user_purchased_feature.UserPurchasedFeatureResponse;

import java.util.List;

public interface UserPurchasedFeatureService {
    List<UserPurchasedFeatureResponse> getUserPurchasedFeatures(Long userId);
    boolean hasRemainingUsage(Long userId, String featureKey);
    Integer getRemainingUsage(Long userId, String featureKey);
    void consumeFeatureUsage(Long userId, String featureKey);
    void addPurchasedFeature(Long userId, String purchasableFeatureId, Integer usageGranted);
}
