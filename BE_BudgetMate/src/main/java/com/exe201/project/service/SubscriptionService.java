package com.exe201.project.service;

import com.exe201.project.dto.request.SubscriptionRequest;
import com.exe201.project.dto.response.SubscriptionResponse;
import com.exe201.project.entity.User;

import java.util.List;

public interface SubscriptionService {
    /**
     * Subscribe user to a membership plan
     * If user has an active subscription, it will be cancelled
     */
    SubscriptionResponse subscribeToMembership(Long membershipPlanId, SubscriptionRequest request);
    
    /**
     * Get user's current active subscription
     */
    SubscriptionResponse getCurrentActiveSubscription(Long userId);
    
    /**
     * Get user's subscription history
     */
    List<SubscriptionResponse> getSubscriptionHistory(Long userId);
    
    /**
     * Cancel user's current active subscription
     */
    void cancelCurrentSubscription(Long userId);
    
    /**
     * Create Basic membership for new user (called during registration)
     */
    void createBasicMembershipForUser(User user);
    
    /**
     * Check and update expired subscriptions (scheduled task)
     */
    void updateExpiredSubscriptions();
}
