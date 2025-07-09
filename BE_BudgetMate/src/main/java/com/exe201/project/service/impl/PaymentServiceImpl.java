package com.exe201.project.service.impl;

import com.exe201.project.dto.request.PaymentRequest;
import com.exe201.project.dto.response.PaymentResponse;
import com.exe201.project.entity.MembershipPlan;
import com.exe201.project.entity.Subscription;
import com.exe201.project.entity.User;
import com.exe201.project.enums.PaymentStatus;
import com.exe201.project.enums.SubscriptionStatus;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.repository.MembershipPlanRepository;
import com.exe201.project.repository.SubscriptionRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private final PayOS payOS;
    private final MembershipPlanRepository membershipPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    
    @Override
    public PaymentResponse createPaymentLink(Long membershipPlanId, PaymentRequest request) {
        try {
            // Get current authenticated user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            // Get membership plan
            MembershipPlan membershipPlan = membershipPlanRepository.findById(membershipPlanId)
                    .orElseThrow(() -> new ResourceNotFoundException("Membership plan not found with id: " + membershipPlanId));
            
            // Check if it's a free plan
            if (membershipPlan.getPrice() == 0.0) {
                throw new IllegalArgumentException("Cannot create payment for free membership plan");
            }
            
            // Cancel existing active subscription
            cancelExistingActiveSubscription(user.getId());
            
            // Create pending subscription
            Subscription pendingSubscription = createPendingSubscription(user, membershipPlan, request);
            
            // Generate unique order code
            String orderCode = generateOrderCode(pendingSubscription.getId());
            
            // Create PayOS payment data
            ItemData item = ItemData.builder()
                    .name(membershipPlan.getName() + " Membership")
                    .quantity(1)
                    .price(membershipPlan.getPrice().intValue())
                    .build();
            
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(Long.parseLong(orderCode))
                    .amount(membershipPlan.getPrice().intValue())
                    .description("Payment for " + membershipPlan.getName() + " membership plan")
                    .items(List.of(item))
                    .returnUrl(request.returnUrl() != null ? request.returnUrl() : "http://localhost:3000/payment/success")
                    .cancelUrl(request.cancelUrl() != null ? request.cancelUrl() : "http://localhost:3000/payment/cancel")
                    .build();
            
            // Create payment link
            CheckoutResponseData checkoutResponse = payOS.createPaymentLink(paymentData);
            
            // Update subscription with order code
            pendingSubscription.setOrderCode(orderCode);
            subscriptionRepository.save(pendingSubscription);
            
            log.info("Created payment link for user {} and membership plan {}", user.getEmail(), membershipPlan.getName());
            
            return PaymentResponse.builder()
                    .orderCode(orderCode)
                    .checkoutUrl(checkoutResponse.getCheckoutUrl())
                    .qrCode(checkoutResponse.getQrCode())
                    .amount(membershipPlan.getPrice())
                    .currency("VND")
                    .status("PENDING")
                    .membershipPlanName(membershipPlan.getName())
                    .subscriptionId(pendingSubscription.getId())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error creating payment link: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment link: " + e.getMessage());
        }
    }
    
    @Override
    public void handlePaymentWebhook(String webhookBody, String signature) {
        try {
            // Verify webhook signature
            // PayOS webhook verification logic here
            
            // Parse webhook data
            // Extract order code and payment status from webhook
            // This is a simplified implementation
            log.info("Received payment webhook: {}", webhookBody);
            
            // You would parse the webhook JSON here and extract the relevant data
            // For now, this is a placeholder implementation
            
        } catch (Exception e) {
            log.error("Error handling payment webhook: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void confirmPayment(String orderCode, String status) {
        try {
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findByOrderCode(orderCode);
            
            if (subscriptionOpt.isEmpty()) {
                log.warn("No subscription found for order code: {}", orderCode);
                return;
            }
            
            Subscription subscription = subscriptionOpt.get();
            
            if ("PAID".equals(status) || "COMPLETED".equals(status)) {
                subscription.setStatus(SubscriptionStatus.ACTIVE);
                subscription.setPaymentStatus(PaymentStatus.COMPLETED);
                subscriptionRepository.save(subscription);
                
                log.info("Payment confirmed and subscription activated for order: {}", orderCode);
            } else if ("CANCELLED".equals(status) || "FAILED".equals(status)) {
                subscription.setStatus(SubscriptionStatus.CANCELLED);
                subscription.setPaymentStatus(PaymentStatus.FAILED);
                subscriptionRepository.save(subscription);
                
                log.info("Payment failed or cancelled for order: {}", orderCode);
            }
            
        } catch (Exception e) {
            log.error("Error confirming payment: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean processAutoRenewalPayment(Subscription subscription) {
        try {
            // For auto-renewal, we would typically store payment method details
            // and charge the saved payment method
            
            // This is a simplified implementation
            if (subscription.getPaymentMethod() == null) {
                log.warn("No payment method stored for auto-renewal of subscription {}", subscription.getId());
                return false;
            }
            
            // Generate order code for auto-renewal
            String orderCode = generateOrderCode(subscription.getId());
            
            // Create payment data for auto-renewal
            ItemData item = ItemData.builder()
                    .name(subscription.getMembershipPlan().getName() + " Auto-Renewal")
                    .quantity(1)
                    .price(subscription.getMembershipPlan().getPrice().intValue())
                    .build();
            
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(Long.parseLong(orderCode))
                    .amount(subscription.getMembershipPlan().getPrice().intValue())
                    .description("Auto-renewal for " + subscription.getMembershipPlan().getName())
                    .items(List.of(item))
                    .build();
            
            // For auto-renewal, you would typically use a stored payment method
            // This is simplified - in reality you'd charge the stored payment method
            log.info("Processing auto-renewal payment for subscription {}", subscription.getId());
            
            // Simulate payment processing (80% success rate)
            double random = Math.random();
            boolean success = random < 0.8;
            
            if (success) {
                log.info("Auto-renewal payment successful for subscription {}", subscription.getId());
                return true;
            } else {
                log.warn("Auto-renewal payment failed for subscription {}", subscription.getId());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error processing auto-renewal payment: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getPaymentStatus(String orderCode) {
        try {
            // Query PayOS for payment status
            // This would use PayOS API to get payment status
            
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findByOrderCode(orderCode);
            if (subscriptionOpt.isPresent()) {
                return subscriptionOpt.get().getPaymentStatus().toString();
            }
            
            return "NOT_FOUND";
        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage(), e);
            return "ERROR";
        }
    }
    
    @Override
    public void cancelPayment(String orderCode, String reason) {
        try {
            // Cancel payment in PayOS
            payOS.cancelPaymentLink(Long.parseLong(orderCode), reason);
            
            // Update subscription status
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findByOrderCode(orderCode);
            if (subscriptionOpt.isPresent()) {
                Subscription subscription = subscriptionOpt.get();
                subscription.setStatus(SubscriptionStatus.CANCELLED);
                subscription.setPaymentStatus(PaymentStatus.CANCELLED);
                subscriptionRepository.save(subscription);
            }
            
            log.info("Payment cancelled for order: {} with reason: {}", orderCode, reason);
        } catch (Exception e) {
            log.error("Error cancelling payment: {}", e.getMessage(), e);
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
    
    private Subscription createPendingSubscription(User user, MembershipPlan membershipPlan, PaymentRequest request) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setMembershipPlan(membershipPlan);
        subscription.setStartDate(LocalDate.now());
        
        // Calculate end date
        if (membershipPlan.getDuration() == 0.0) {
            subscription.setEndDate(LocalDate.now().plusYears(100));
        } else {
            subscription.setEndDate(LocalDate.now().plusMonths(membershipPlan.getDuration().longValue()));
        }
        
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setPaymentMethod(request.paymentMethod());
        subscription.setPaymentStatus(PaymentStatus.PENDING);
        
        return subscriptionRepository.save(subscription);
    }
    
    private String generateOrderCode(Long subscriptionId) {
        // Generate unique order code based on subscription ID and timestamp
        long timestamp = new Date().getTime();
        return String.valueOf(subscriptionId * 1000 + (timestamp % 1000));
    }
}
