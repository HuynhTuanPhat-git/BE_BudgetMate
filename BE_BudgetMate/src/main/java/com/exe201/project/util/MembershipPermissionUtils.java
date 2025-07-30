package com.exe201.project.util;

import com.exe201.project.service.MembershipAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipPermissionUtils {
    
    private final MembershipAccessService membershipAccessService;
    
    public void checkWalletCreationPermission(Long userId) {
        if (!membershipAccessService.canCreateSavingsWallets(userId)) {
            throw new SecurityException("You don't have permission to create savings wallets");
        }
    }
    
    public void checkMultipleWalletPermission(Long userId) {
        if (!membershipAccessService.canCreateDeptWallets(userId)) {
            throw new SecurityException("You don't have permission to create dept wallets");
        }
    }
    
    public void checkAdvancedAnalyticsPermission(Long userId) {
        if (!membershipAccessService.hasAdvancedAnalytics(userId)) {
            throw new SecurityException("Advanced analytics requires Premium membership");
        }
    }
    
    public void checkExportPermission(Long userId) {
        if (!membershipAccessService.canExportData(userId)) {
            throw new SecurityException("Data export requires Plus or Premium membership");
        }
    }
    
    public boolean canUserAccessFeature(Long userId, String featureKey) {
        return membershipAccessService.hasFeatureAccess(userId, featureKey);
    }
    
    public Integer getUserFeatureLimit(Long userId, String featureKey) {
        return membershipAccessService.getFeatureLimit(userId, featureKey);
    }
}
