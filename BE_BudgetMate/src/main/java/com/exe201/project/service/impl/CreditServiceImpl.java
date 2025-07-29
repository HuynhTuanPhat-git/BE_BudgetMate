package com.exe201.project.service.impl;

import com.exe201.project.dto.request.feature.PurchaseFeatureRequest;
import com.exe201.project.dto.response.PagedResponse;
import com.exe201.project.dto.response.credit_transaction.CreditTransactionResponse;
import com.exe201.project.dto.response.feature.FeaturePurchaseResponse;
import com.exe201.project.entity.*;
import com.exe201.project.exception.BadRequestException;
import com.exe201.project.exception.InsufficientBalanceException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.repository.*;
import com.exe201.project.service.CreditService;
import com.exe201.project.service.MembershipPlanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreditServiceImpl implements CreditService {

    private final UserRepository userRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PurchasableFeatureRepository purchasableFeatureRepository;
    private final MembershipPlanService membershipPlanService;

    @Override
    public FeaturePurchaseResponse purchaseFeature(Long userId, PurchaseFeatureRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        PurchasableFeature purchasableFeature = purchasableFeatureRepository.findById(request.purchasableFeatureId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchasable feature not found"));

        if (!purchasableFeature.getIsActive()) {
            throw new BadRequestException("This feature is not available for purchase");
        }

        Optional<Subscription> currentSubscription = subscriptionRepository
                .findActiveSubscriptionByUserId(userId, LocalDate.now());

        if (currentSubscription.isEmpty()) {
            throw new BadRequestException("User must have an active subscription to purchase features");
        }

        String currentPlanName = currentSubscription.get().getMembershipPlan().getName();

        if ("Premium".equals(currentPlanName)) {
            throw new BadRequestException("Premium users have access to all features");
        }

        if (!canPlanPurchaseFeature(purchasableFeature, currentPlanName)) {
            throw new BadRequestException("This feature is not available for your current membership plan");
        }

        boolean hasFeatureInPlan = membershipPlanService.hasFeatureAccess(
                currentSubscription.get().getMembershipPlan().getId(),
                purchasableFeature.getFeature().getFeatureKey());

        if (hasFeatureInPlan) {
            throw new BadRequestException("You already have access to this feature through your membership plan");
        }

        Integer creditPrice = purchasableFeature.getCreditPrice();
        if (user.getCredits() < creditPrice) {
            throw new InsufficientBalanceException("Insufficient credits. Required: " + creditPrice + ", Available: " + user.getCredits());
        }

        user.setCredits(user.getCredits() - creditPrice);
        userRepository.save(user);

        CreditTransaction transaction = new CreditTransaction();
        transaction.setUser(user);
        transaction.setPurchasableFeature(purchasableFeature);
        transaction.setCreditSpent(creditPrice);
        transaction.setUsageGranted(purchasableFeature.getUsageLimit());
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setDescription("Purchased feature: " + purchasableFeature.getFeature().getName());

        creditTransactionRepository.save(transaction);

        log.info("User {} purchased feature {} for {} credits",
                userId, purchasableFeature.getFeature().getFeatureKey(), creditPrice);

        return FeaturePurchaseResponse.builder()
                .featureName(purchasableFeature.getFeature().getName())
                .featureKey(purchasableFeature.getFeature().getFeatureKey())
                .creditSpent(creditPrice)
                .remainingCredits(user.getCredits())
                .message("Feature purchased successfully!")
                .build();
    }

    private boolean canPlanPurchaseFeature(PurchasableFeature purchasableFeature, String planName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> targetPlans = objectMapper.readValue(
                    purchasableFeature.getTargetMembershipPlans(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            return targetPlans.contains(planName);
        } catch (Exception e) {
            log.error("Error parsing target membership plans: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CreditTransactionResponse> getCreditTransactionHistory(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Page<CreditTransaction> transactionPage = creditTransactionRepository
                .findByUserOrderByTransactionTimeDesc(user, pageable);

        Page<CreditTransactionResponse> responsePage = transactionPage.map(transaction ->
                CreditTransactionResponse.builder()
                        .id(transaction.getId())
                        .featureName(transaction.getPurchasableFeature().getFeature().getName())
                        .featureKey(transaction.getPurchasableFeature().getFeature().getFeatureKey())
                        .creditSpent(transaction.getCreditSpent())
                        .transactionTime(transaction.getTransactionTime())
                        .description(transaction.getDescription())
                        .build()
        );

        return new PagedResponse<>(responsePage);
    }
}