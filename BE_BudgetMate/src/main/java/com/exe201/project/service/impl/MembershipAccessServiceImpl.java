package com.exe201.project.service.impl;

import com.exe201.project.entity.Subscription;
import com.exe201.project.entity.User;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.repository.CreditTransactionRepository;
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
    private final CreditTransactionRepository creditTransactionRepository;

    @Override
    public boolean hasFeatureAccess(Long userId, String featureKey) {
        Optional<Subscription> activeSubscription = getActiveSubscription(userId);

        if (activeSubscription.isEmpty()) {
            return isBasicFeature(featureKey);
        }

        Subscription subscription = activeSubscription.get();
        String planName = subscription.getMembershipPlan().getName();

        if ("Premium".equals(planName)) {
            return true;
        }

        boolean hasFeatureInPlan = membershipPlanService.hasFeatureAccess(
                subscription.getMembershipPlan().getId(), featureKey);

        if (hasFeatureInPlan) {
            return true;
        }

        if ("Basic".equals(planName) || "Plus".equals(planName)) {
            return hasUserPurchasedFeature(userId, featureKey);
        }

        return false;
    }

    @Override
    public Integer getFeatureLimit(Long userId, String featureKey) {
        Optional<Subscription> activeSubscription = getActiveSubscription(userId);

        if (activeSubscription.isEmpty()) {
            return getBasicFeatureLimit(featureKey);
        }

        Subscription subscription = activeSubscription.get();
        String planName = subscription.getMembershipPlan().getName();

        if ("Premium".equals(planName)) {
            return null;
        }

        Integer planLimit = membershipPlanService.getFeatureLimit(
                subscription.getMembershipPlan().getId(), featureKey);

        if ("Basic".equals(planName) || "Plus".equals(planName)) {
            Long purchaseCount = getFeaturePurchaseCount(userId, featureKey);

            if (purchaseCount > 0) {
                int currentLimit = (planLimit != null) ? planLimit : 0;
                return currentLimit + purchaseCount.intValue();
            }
        }

        return planLimit;
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

    private boolean hasUserPurchasedFeature(Long userId, String featureKey) {
        Long purchaseCount = getFeaturePurchaseCount(userId, featureKey);
        return purchaseCount > 0;
    }

    private Long getFeaturePurchaseCount(Long userId, String featureKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return creditTransactionRepository.countFeaturePurchasesByUser(user, featureKey.toUpperCase());
    }

    private Optional<Subscription> getActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveSubscriptionByUserId(userId, LocalDate.now());
    }

    private boolean isBasicFeature(String featureKey) {
        return switch (featureKey.toUpperCase()) {
            case "CREATE_WALLET" -> true;
            case "CREATE_MULTIPLE_WALLETS" -> true;
            case "BASIC_TRANSACTIONS" -> true;
            default -> false;
        };
    }

    private Integer getBasicFeatureLimit(String featureKey) {
        return switch (featureKey.toUpperCase()) {
            case "CREATE_WALLET" -> 3;
            case "CREATE_MULTIPLE_WALLETS" -> 3;
            case "TRANSACTIONS_PER_MONTH" -> 50;
            default -> 0;
        };
    }
}