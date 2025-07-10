# PayOS Payment Integration Implementation

## Overview
This implementation integrates PayOS payment gateway into the BudgetMate backend application for handling membership plan subscriptions.

## Components Implemented

### 1. Core Payment Service (`PaymentService` & `PaymentServiceImpl`)
- **PaymentService Interface**: Defines contract for payment operations
- **PaymentServiceImpl**: Implements PayOS integration with the following methods:
  - `createPaymentLink()`: Creates payment links for membership plans
  - `handlePaymentWebhook()`: Processes payment webhooks from PayOS
  - `confirmPayment()`: Confirms payment status and updates subscription
  - `processAutoRenewalPayment()`: Handles auto-renewal payments
  - `getPaymentStatus()`: Retrieves payment status from PayOS
  - `cancelPayment()`: Cancels payment links

### 2. Payment Controller (`PaymentController`)
- **Endpoints**:
  - `POST /api/v1/payment/create/{membershipPlanId}`: Create payment link
  - `GET /api/v1/payment/status/{orderCode}`: Get payment status
  - `DELETE /api/v1/payment/cancel/{orderCode}`: Cancel payment
  - `POST /api/v1/payment/webhook`: Handle PayOS webhooks
  - `POST /api/v1/payment/confirm`: Manual payment confirmation (testing)

### 3. Enhanced Subscription Service
- **Updated SubscriptionServiceImpl**:
  - Added PaymentService integration
  - Differentiated between free and paid membership handling
  - Added default wallet creation for Basic plan users
  - Integrated auto-renewal with PayOS payment processing

### 4. Enhanced Subscription Controller
- **Added endpoint**:
  - `POST /api/v1/subscriptions/payment/{membershipPlanId}`: Initiate payment for paid memberships

### 5. Database Updates
- **Subscription Entity**: Added `orderCode` field for payment tracking
- **SubscriptionRepository**: Added `findByOrderCode()` method

### 6. Dependencies
- **Added PayOS SDK**: `vn.payos:payos-java:1.0.3`
- **Added Gson**: `com.google.code.gson:gson:2.10.1`

## Payment Flow

### For Free Memberships (Basic Plan):
1. User subscribes directly via `/api/v1/subscriptions/subscribe/{membershipPlanId}`
2. System creates active subscription immediately
3. Default wallets created for Basic plan users

### For Paid Memberships:
1. User initiates payment via `/api/v1/payment/create/{membershipPlanId}`
2. System creates pending subscription
3. PayOS payment link generated and returned
4. User completes payment on PayOS platform
5. PayOS sends webhook to `/api/v1/payment/webhook`
6. System processes webhook and activates subscription

## PayOS Integration Details

### PaymentData Structure:
```java
PaymentData paymentData = PaymentData.builder()
    .orderCode(Long.parseLong(orderCode))
    .amount(membershipPlan.getPrice().intValue())
    .description("Payment for " + membershipPlan.getName() + " membership plan")
    .items(List.of(item))
    .returnUrl(request.returnUrl())
    .cancelUrl(request.cancelUrl())
    .build();
```

### Webhook Processing:
- Verifies webhook signature using PayOS SDK
- Extracts payment status and order code
- Updates subscription status based on payment result
- Handles both successful and failed payments

### Auto-Renewal:
- Integrated with PayOS for automatic subscription renewal
- Fallback to Basic plan if renewal fails
- Configurable success rates for testing

## API Endpoints Summary

### Payment Endpoints:
- `POST /api/v1/payment/create/{membershipPlanId}` - Create payment link
- `GET /api/v1/payment/status/{orderCode}` - Get payment status
- `DELETE /api/v1/payment/cancel/{orderCode}` - Cancel payment
- `POST /api/v1/payment/webhook` - PayOS webhook handler

### Subscription Endpoints:
- `POST /api/v1/subscriptions/subscribe/{membershipPlanId}` - Direct subscription (free plans)
- `POST /api/v1/subscriptions/payment/{membershipPlanId}` - Payment initiation (paid plans)
- `GET /api/v1/subscriptions/current` - Get current subscription
- `GET /api/v1/subscriptions/history` - Get subscription history

### Admin Endpoints:
- `POST /api/v1/subscriptions/admin/process-expired` - Process expired subscriptions
- `POST /api/v1/subscriptions/admin/set-renewal-rate/{rate}` - Set auto-renewal rate

## Configuration Required

### PayOS Configuration:
Add to `application.properties`:
```properties
payos.client-id=YOUR_CLIENT_ID
payos.api-key=YOUR_API_KEY
payos.checksum-key=YOUR_CHECKSUM_KEY
```

### PayOS Bean Configuration:
The `PayOsConfig` class provides PayOS bean configuration using the above properties.

## Testing

### Test Endpoints:
- `GET /api/v1/test/payment-integration` - Test integration status

### Manual Testing:
1. Create payment link for paid membership
2. Use PayOS test environment for payment
3. Verify webhook processing
4. Check subscription activation

## Security Notes

1. **Webhook Security**: PayOS webhooks are verified using signature validation
2. **Authentication**: All user endpoints require bearer token authentication
3. **Admin Endpoints**: Should add `@PreAuthorize("hasRole('ROLE_ADMIN')")` for production
4. **Error Handling**: Comprehensive error handling with logging

## Next Steps

1. **Production Configuration**: Set up PayOS production credentials
2. **Webhook URL**: Configure webhook URL in PayOS dashboard
3. **Testing**: Comprehensive testing with PayOS sandbox
4. **Security**: Add role-based authorization for admin endpoints
5. **Monitoring**: Add payment monitoring and alerting
6. **Documentation**: API documentation with Swagger/OpenAPI

## Error Handling

The implementation includes comprehensive error handling:
- PayOS API errors are caught and logged
- Webhook processing failures are logged but don't break the system
- Payment failures trigger appropriate subscription status updates
- Auto-renewal failures fallback to Basic plan

## Logging

All payment operations are logged with appropriate levels:
- `INFO`: Successful operations
- `WARN`: Payment failures, missing data
- `ERROR`: System errors, API failures

This implementation provides a complete PayOS integration for the BudgetMate membership system with proper error handling, webhook processing, and auto-renewal capabilities.
