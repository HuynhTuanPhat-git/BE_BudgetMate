package com.exe201.project.service.impl;

import com.exe201.project.entity.Subscription;
import com.exe201.project.repository.SubscriptionRepository;
import com.exe201.project.service.MembershipAccessService;
import com.exe201.project.service.MembershipPlanService;
import com.exe201.project.service.UserPurchasedFeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipAccessServiceImpl implements MembershipAccessService {

    private final SubscriptionRepository subscriptionRepository;
    private final MembershipPlanService membershipPlanService;
    private final UserPurchasedFeatureService userPurchasedFeatureService;

    @Override
    public boolean hasFeatureAccess(Long userId, String featureKey) {
        Optional<Subscription> activeSubscription = getActiveSubscription(userId);
        if (activeSubscription.isEmpty()) {
            return isBasicFeature(featureKey);
        }

        Subscription subscription = activeSubscription.get();
        String planName = subscription.getMembershipPlan().getName();

        if ("Premium".equalsIgnoreCase(planName)) {
            return true;
        }

        return membershipPlanService.hasFeatureAccess(
                subscription.getMembershipPlan().getId(), featureKey);
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
            Integer remainingUsage = userPurchasedFeatureService.getRemainingUsage(userId, featureKey);

            if (remainingUsage > 0) {
                int currentLimit = (planLimit != null) ? planLimit : 0;
                return currentLimit + remainingUsage;
            }
        }

        return planLimit;
    }

    @Override
    public boolean canCreateSavingsWallets(Long userId) {
        return hasFeatureAccess(userId, "CREATE_SAVINGS_WALLETS");
    }

    @Override
    public boolean canCreateDeptWallets(Long userId) {
        return hasFeatureAccess(userId, "CREATE_DEPT_WALLETS");
    }

    @Override
    public boolean hasAdvancedAnalytics(Long userId) {
        return hasFeatureAccess(userId, "ADVANCED_ANALYTICS");
    }

    @Override
    public boolean canExportData(Long userId) {
        return hasFeatureAccess(userId, "EXPORT_DATA");
    }

    @Override
    public Integer getMembershipPlanLimit(Long userId, String featureKey) {
        Optional<Subscription> activeSubscription = getActiveSubscription(userId);

        if (activeSubscription.isEmpty()) {
            return getBasicFeatureLimit(featureKey);
        }

        Subscription subscription = activeSubscription.get();
        String planName = subscription.getMembershipPlan().getName();

        if ("Premium".equals(planName)) {
            return null;
        }

        return membershipPlanService.getFeatureLimit(
                subscription.getMembershipPlan().getId(), featureKey);
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
            case "CREATE_SAVINGS_WALLETS" -> 1;
            case "CREATE_DEPT_WALLETS" -> 1;
            default -> 0;
        };
    }
}