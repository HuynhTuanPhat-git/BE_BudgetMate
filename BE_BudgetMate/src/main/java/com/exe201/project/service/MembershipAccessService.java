package com.exe201.project.service;

public interface MembershipAccessService {
    boolean hasFeatureAccess(Long userId, String featureKey);
    Integer getFeatureLimit(Long userId, String featureKey);
    boolean canCreateWallet(Long userId);
    boolean canCreateMultipleWallets(Long userId);
    boolean hasAdvancedAnalytics(Long userId);
    boolean canExportData(Long userId);
}
