package com.exe201.project.service.impl;

import com.exe201.project.dto.request.feature.PurchaseFeatureRequest;
import com.exe201.project.dto.response.PagedResponse;
import com.exe201.project.dto.response.credit_transaction.CreditTransactionResponse;
import com.exe201.project.dto.response.feature.FeaturePurchaseResponse;
import com.exe201.project.entity.CreditTransaction;
import com.exe201.project.entity.MembershipFeature;
import com.exe201.project.entity.Subscription;
import com.exe201.project.entity.User;
import com.exe201.project.exception.BadRequestException;
import com.exe201.project.exception.InsufficientBalanceException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.repository.CreditTransactionRepository;
import com.exe201.project.repository.MembershipFeatureRepository;
import com.exe201.project.repository.SubscriptionRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreditServiceImpl implements CreditService {

    private final UserRepository userRepository;
    private final MembershipFeatureRepository membershipFeatureRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public FeaturePurchaseResponse purchaseFeature(Long userId, PurchaseFeatureRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        MembershipFeature membershipFeature = membershipFeatureRepository.findById(request.membershipFeatureId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership feature not found with id: " + request.membershipFeatureId()));

        Optional<Subscription> currentSubscription = subscriptionRepository
                .findActiveSubscriptionByUserId(userId, java.time.LocalDate.now());

        if (currentSubscription.isEmpty()) {
            throw new BadRequestException("User must have an active subscription to purchase features");
        }

        String currentPlanName = currentSubscription.get().getMembershipPlan().getName();

        if ("Premium".equals(currentPlanName)) {
            throw new BadRequestException("Premium users have access to all features");
        }

        Long userMembershipPlanId = currentSubscription.get().getMembershipPlan().getId();
        Long featureMembershipPlanId = membershipFeature.getMembershipPlan().getId();

        if (!userMembershipPlanId.equals(featureMembershipPlanId)) {
            throw new BadRequestException("This feature does not belong to your current membership plan");
        }

        if (membershipFeature.getCreditPrice() == null || membershipFeature.getCreditPrice() <= 0) {
            throw new BadRequestException("This feature cannot be purchased with credits");
        }

        Integer creditPrice = membershipFeature.getCreditPrice();
        if (user.getCredits() < creditPrice) {
            throw new InsufficientBalanceException("Insufficient credits. Required: " + creditPrice + ", Available: " + user.getCredits());
        }

        user.setCredits(user.getCredits() - creditPrice);
        userRepository.save(user);

        CreditTransaction transaction = new CreditTransaction();
        transaction.setUser(user);
        transaction.setMembershipFeature(membershipFeature);
        transaction.setCreditSpent(creditPrice);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setDescription("Purchased feature: " + membershipFeature.getFeature().getName());

        creditTransactionRepository.save(transaction);

        log.info("User {} purchased feature {} for {} credits",
                userId, membershipFeature.getFeature().getFeatureKey(), creditPrice);

        return FeaturePurchaseResponse.builder()
                .featureName(membershipFeature.getFeature().getName())
                .featureKey(membershipFeature.getFeature().getFeatureKey())
                .creditSpent(creditPrice)
                .remainingCredits(user.getCredits())
                .message("Feature purchased successfully!")
                .build();
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
                        .featureName(transaction.getMembershipFeature().getFeature().getName())
                        .featureKey(transaction.getMembershipFeature().getFeature().getFeatureKey())
                        .creditSpent(transaction.getCreditSpent())
                        .transactionTime(transaction.getTransactionTime())
                        .description(transaction.getDescription())
                        .build()
        );
        return new PagedResponse<>(responsePage);
    }
}
