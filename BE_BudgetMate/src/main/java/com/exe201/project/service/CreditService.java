package com.exe201.project.service;

import com.exe201.project.dto.request.feature.PurchaseFeatureRequest;
import com.exe201.project.dto.response.PagedResponse;
import com.exe201.project.dto.response.credit_transaction.CreditTransactionResponse;
import com.exe201.project.dto.response.feature.FeaturePurchaseResponse;
import org.springframework.data.domain.Pageable;

public interface CreditService {
    FeaturePurchaseResponse purchaseFeature(Long userId, PurchaseFeatureRequest request);
    PagedResponse<CreditTransactionResponse> getCreditTransactionHistory(Long userId, Pageable pageable);
}
