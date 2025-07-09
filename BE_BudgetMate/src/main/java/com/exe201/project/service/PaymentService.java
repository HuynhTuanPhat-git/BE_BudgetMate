package com.exe201.project.service;

import com.exe201.project.dto.request.PaymentRequest;
import com.exe201.project.dto.response.PaymentResponse;
import com.exe201.project.entity.Subscription;

public interface PaymentService {
    /**
     * Create payment link for subscription
     */
    PaymentResponse createPaymentLink(Long membershipPlanId, PaymentRequest request);
    
    /**
     * Handle payment webhook from PayOS
     */
    void handlePaymentWebhook(String webhookBody, String signature);
    
    /**
     * Confirm payment and activate subscription
     */
    void confirmPayment(String orderCode, String status);
    
    /**
     * Process auto-renewal payment
     */
    boolean processAutoRenewalPayment(Subscription subscription);
    
    /**
     * Get payment status
     */
    String getPaymentStatus(String orderCode);
    
    /**
     * Cancel payment
     */
    void cancelPayment(String orderCode, String reason);
}
