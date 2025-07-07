package com.exe201.project.service.impl;

import com.exe201.project.dto.request.SubscriptionRequest;
import com.exe201.project.dto.response.SubscriptionResponse;
import com.exe201.project.entity.MembershipPlan;
import com.exe201.project.entity.Subscription;
import com.exe201.project.entity.User;
import com.exe201.project.entity.Wallets;
import com.exe201.project.enums.PaymentStatus;
import com.exe201.project.enums.SubscriptionStatus;
import com.exe201.project.enums.WalletType;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.mapper.SubscriptionMapper;
import com.exe201.project.repository.MembershipPlanRepository;
import com.exe201.project.repository.SubscriptionRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.repository.WalletRepository;
import com.exe201.project.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final SubscriptionMapper subscriptionMapper;
    
    @Override
    public SubscriptionResponse subscribeToMembership(Long membershipPlanId, SubscriptionRequest request) {
        // Get current authenticated user from security context
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Find the membership plan
        MembershipPlan membershipPlan = membershipPlanRepository.findById(membershipPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + membershipPlanId));
        
        // Cancel any existing active subscription
        cancelExistingActiveSubscription(user.getId());
        
        // Create new subscription
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setMembershipPlan(membershipPlan);
        subscription.setStartDate(LocalDate.now());
        
        // Calculate end date based on membership plan duration
        if (membershipPlan.getDuration() == 0.0) {
            // No expiration (Basic plan)
            subscription.setEndDate(LocalDate.now().plusYears(100));
        } else {
            // Add duration in months
            subscription.setEndDate(LocalDate.now().plusMonths(membershipPlan.getDuration().longValue()));
        }
        
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPaymentMethod(request.paymentMethod());
        subscription.setPaymentStatus(PaymentStatus.COMPLETED); // For simplicity, assume payment is always completed
        
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        
        // If subscribing to Basic plan for the first time, create default wallets
//        if ("Basic".equals(membershipPlan.getName()) && !hasWallets(user.getId())) {
//            createDefaultWalletsForUser(user);
//        }
        
        log.info("User {} subscribed to membership plan: {}", user.getEmail(), membershipPlan.getName());
        
        return subscriptionMapper.toSubscriptionResponse(savedSubscription);
    }
    
    @Override
    public SubscriptionResponse getCurrentActiveSubscription(Long userId) {
        Optional<Subscription> activeSubscription = subscriptionRepository
                .findActiveSubscriptionByUserId(userId, LocalDate.now());
        
        return activeSubscription
                .map(subscriptionMapper::toSubscriptionResponse)
                .orElse(null);
    }
    
    @Override
    public List<SubscriptionResponse> getSubscriptionHistory(Long userId) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        return subscriptions.stream()
                .map(subscriptionMapper::toSubscriptionResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public void cancelCurrentSubscription(Long userId) {
        Optional<Subscription> activeSubscription = subscriptionRepository
                .findActiveSubscriptionByUserId(userId, LocalDate.now());
        
        if (activeSubscription.isPresent()) {
            Subscription subscription = activeSubscription.get();
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(subscription);
            log.info("Cancelled subscription for user ID: {}", userId);
        }
    }
    
    @Override
    public void createBasicMembershipForUser(User user) {
        try {
            // Find the Basic membership plan
            MembershipPlan basicPlan = membershipPlanRepository.findByName("Basic")
                    .orElseThrow(() -> new ResourceNotFoundException("Basic membership plan not found"));
            
            // Check if user already has an active subscription
            Optional<Subscription> existingSubscription = subscriptionRepository
                    .findActiveSubscriptionByUserId(user.getId(), LocalDate.now());
            
            if (existingSubscription.isPresent()) {
                log.info("User {} already has an active subscription", user.getEmail());
                return;
            }
            
            // Create Basic subscription
            Subscription subscription = new Subscription();
            subscription.setUser(user);
            subscription.setMembershipPlan(basicPlan);
            subscription.setStartDate(LocalDate.now());
            subscription.setEndDate(LocalDate.now().plusYears(100)); // No expiration for Basic
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setPaymentMethod(null); // Free plan
            subscription.setPaymentStatus(PaymentStatus.COMPLETED);
            subscriptionRepository.save(subscription);
            
            // Create default wallets
//            createDefaultWalletsForUser(user);
            
            log.info("Created Basic membership and default wallets for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to create Basic membership for user {}: {}", user.getEmail(), e.getMessage());
            // Don't throw exception as user registration should still succeed
        }
    }
    
    @Override
    @Scheduled(cron = "0 0 1 * * ?") // Run daily at 1 AM
    public void updateExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findExpiredActiveSubscriptions(LocalDate.now());
        
        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            log.info("Marked subscription {} as EXPIRED for user {}", 
                    subscription.getId(), subscription.getUser().getEmail());
        }
        
        if (!expiredSubscriptions.isEmpty()) {
            log.info("Updated {} expired subscriptions", expiredSubscriptions.size());
        }
    }
    
    private void cancelExistingActiveSubscription(Long userId) {
        Optional<Subscription> activeSubscription = subscriptionRepository
                .findActiveSubscriptionByUserId(userId, LocalDate.now());
        
        if (activeSubscription.isPresent()) {
            Subscription subscription = activeSubscription.get();
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(subscription);
            log.info("Cancelled existing active subscription for user ID: {}", userId);
        }
    }
    
    private boolean hasWallets(Long userId) {
        return !walletRepository.findAllByUserId(userId).isEmpty();
    }
    
    private void createDefaultWalletsForUser(User user) {
        // Create DEFAULT wallet
        Wallets defaultWallet = new Wallets();
        defaultWallet.setName("My Wallet");
        defaultWallet.setType(WalletType.DEFAULT);
        defaultWallet.setBalance(0.0);
        defaultWallet.setTargetAmount(0.0);
        defaultWallet.setInterestRate(0.0);
        defaultWallet.setDeadline(null);
        defaultWallet.setUser(user);
        defaultWallet.setHidden(false);
        walletRepository.save(defaultWallet);

        // Create DEBT wallet
        Wallets debtWallet = new Wallets();
        debtWallet.setName("Debt Tracker");
        debtWallet.setType(WalletType.DEBT);
        debtWallet.setBalance(0.0);
        debtWallet.setTargetAmount(0.0); // Goal to pay off debt
        debtWallet.setInterestRate(0.0);
        debtWallet.setDeadline(null);
        debtWallet.setUser(user);
        debtWallet.setHidden(false);
        walletRepository.save(debtWallet);

        // Create SAVINGS wallet
        Wallets savingsWallet = new Wallets();
        savingsWallet.setName("Savings Goal");
        savingsWallet.setType(WalletType.SAVINGS);
        savingsWallet.setBalance(0.0);
        savingsWallet.setTargetAmount(1000000.0); // Default savings goal
        savingsWallet.setInterestRate(0.0);
        savingsWallet.setDeadline(LocalDate.now().plusYears(1)); // 1 year goal
        savingsWallet.setUser(user);
        savingsWallet.setHidden(false);
        walletRepository.save(savingsWallet);

        log.info("Created default wallets (DEFAULT, DEBT, SAVINGS) for user: {}", user.getEmail());
    }
}
