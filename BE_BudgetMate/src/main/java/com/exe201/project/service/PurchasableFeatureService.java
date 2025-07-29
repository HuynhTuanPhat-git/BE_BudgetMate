package com.exe201.project.service;

import com.exe201.project.dto.request.feature.CreatePurchasableFeatureRequest;
import com.exe201.project.dto.request.feature.UpdatePurchasableFeatureRequest;
import com.exe201.project.dto.response.PagedResponse;
import com.exe201.project.dto.response.purchasable_feature.PurchasableFeatureResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PurchasableFeatureService {
    PurchasableFeatureResponse createPurchasableFeature(CreatePurchasableFeatureRequest request);
    PurchasableFeatureResponse updatePurchasableFeature(UUID id, UpdatePurchasableFeatureRequest request);
    void deletePurchasableFeature(UUID id);
    PurchasableFeatureResponse getPurchasableFeature(UUID id);
    PagedResponse<PurchasableFeatureResponse> getPurchasableFeatures(Pageable pageable);
    List<PurchasableFeatureResponse> getAvailableFeaturesForUser(Long userId);
    List<PurchasableFeatureResponse> getFeatures();
}