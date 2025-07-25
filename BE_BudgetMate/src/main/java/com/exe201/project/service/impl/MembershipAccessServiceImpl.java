package com.exe201.project.service.impl;

import com.exe201.project.entity.Subscription;
import com.exe201.project.entity.User;
import com.exe201.project.enums.SubscriptionStatus;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.repository.SubscriptionRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.MembershipAccessService;
import com.exe201.project.service.MembershipPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipAccessServiceImpl implements MembershipAccessService {
    
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MembershipPlanService membershipPlanService;

    @Override
    public boolean hasFeatureAccess(Long userId, String featureKey) {
        Optional<Subscription> activeSubscription = getActiveSubscription(userId);

        // User has no active subscription, check if it's a free feature
        return activeSubscription.map(subscription -> membershipPlanService.hasFeatureAccess(
                subscription.getMembershipPlan().getId(),
                featureKey
        )).orElseGet(() -> isBasicFeature(featureKey));

    }

    @Override
    public Integer getFeatureLimit(Long userId, String featureKey) {
        Optional<Subscription> activeSubscription = getActiveSubscription(userId);
        
        if (activeSubscription.isEmpty()) {
            // User has no active subscription, return basic limits
            return getBasicFeatureLimit(featureKey);
        }
        
        return membershipPlanService.getFeatureLimit(
            activeSubscription.get().getMembershipPlan().getId(), 
            featureKey
        );
    }

    @Override
    public boolean canCreateWallet(Long userId) {
        return hasFeatureAccess(userId, "CREATE_WALLET");
    }

    @Override
    public boolean canCreateMultipleWallets(Long userId) {
        return hasFeatureAccess(userId, "CREATE_MULTIPLE_WALLETS");
    }

    @Override
    public boolean hasAdvancedAnalytics(Long userId) {
        return hasFeatureAccess(userId, "ADVANCED_ANALYTICS");
    }

    @Override
    public boolean canExportData(Long userId) {
        return hasFeatureAccess(userId, "EXPORT_DATA");
    }
    
    private Optional<Subscription> getActiveSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return subscriptionRepository.findActiveSubscriptionByUserId(userId, LocalDate.now());
    }
    
    private boolean isBasicFeature(String featureKey) {
        // Define which features are available for free users
        return switch (featureKey.toUpperCase()) {
            case "CREATE_WALLET" -> true;
            case "CREATE_MULTIPLE_WALLETS" -> true; // Allow Basic users to create different wallet types
            case "BASIC_TRANSACTIONS" -> true;
            default -> false;
        };
    }
    
    private Integer getBasicFeatureLimit(String featureKey) {
        // Define limits for free users
        return switch (featureKey.toUpperCase()) {
            case "CREATE_WALLET" -> 3; // Free users can create 3 wallets (1 of each type)
            case "CREATE_MULTIPLE_WALLETS" -> 3; // Can create different wallet types
            case "TRANSACTIONS_PER_MONTH" -> 50; // Free users get 50 transactions per month
            default -> 0;
        };
    }
}
