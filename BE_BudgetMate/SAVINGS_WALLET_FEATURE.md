# SAVINGS Wallet with Term Deposits Feature

## Overview
Tính năng gửi tiết kiệm có kì hạn cho phép người dùng tạo ví SAVINGS với ngày bắt đầu và kì hạn cụ thể. Hệ thống sẽ tự động tính toán lãi suất và chuyển tiền về ví DEFAULT khi hết kì hạn.

## Key Features

### 1. Enhanced SAVINGS Wallet Creation
Khi tạo ví SAVINGS, người dùng cần nhập thêm:
- **Start Date** (startDate): Ngày bắt đầu gửi tiết kiệm
- **Term in Months** (termMonths): Kì hạn tính bằng tháng (tối thiểu 1 tháng)
- **Interest Rate**: Lãi suất hàng năm (bắt buộc > 0 cho ví SAVINGS)

### 2. Automatic Maturity Processing
- **Daily Check**: Hệ thống kiểm tra hàng ngày lúc 00:00 (midnight)
- **Status Update**: Ví SAVINGS hết hạn sẽ được chuyển từ `ACTIVE` sang `DONE`
- **Automatic Transfer**: Tiền sau kì hạn sẽ được chuyển vào ví DEFAULT
- **Transaction Record**: Tạo giao dịch ghi nhận việc chuyển tiền

## Database Changes

### 1. Wallets Entity Updates
```java
@Enumerated(EnumType.STRING)
WalletStatus status = WalletStatus.ACTIVE;  // New status field

@Column(name = "start_date")
LocalDate startDate;                        // Ngày bắt đầu gửi tiết kiệm

@Column(name = "term_months")
Integer termMonths;                         // Kì hạn tính bằng tháng
```

### 2. New Enum: WalletStatus
```java
public enum WalletStatus {
    ACTIVE,     // Ví đang hoạt động
    DONE,       // Ví SAVINGS đã hết kì hạn
    INACTIVE    // Ví không hoạt động
}
```

## API Changes

### 1. Enhanced WalletRequest
```json
{
  "type": "SAVINGS",
  "name": "Tiết kiệm 6 tháng",
  "targetAmount": 10000000,
  "interestRate": 8.5,
  "startDate": "2025-07-17",
  "termMonths": 6
}
```

### 2. Enhanced WalletResponse
```json
{
  "id": 1,
  "type": "SAVINGS",
  "status": "ACTIVE",
  "name": "Tiết kiệm 6 tháng",
  "balance": 0,
  "targetAmount": 10000000,
  "interestRate": 8.5,
  "deadline": "2026-01-17",    // Tự động tính từ startDate + termMonths
  "startDate": "2025-07-17",
  "termMonths": 6
}
```

### 3. New Admin Endpoint
```
POST /api/v1/wallet/process-expired-savings
```
- Requires ADMIN role
- Manually trigger processing of expired SAVINGS wallets
- Useful for testing and emergency processing

## Interest Calculation

### Formula
Sử dụng công thức lãi đơn:
```
Maturity Amount = Principal × (1 + (Annual Rate / 100) × (Term Months / 12))
```

### Example
- **Principal**: 10,000,000 VND
- **Interest Rate**: 8.5% per year
- **Term**: 6 months
- **Calculation**: 10,000,000 × (1 + (8.5/100) × (6/12)) = 10,425,000 VND

## Validation Rules

### SAVINGS Wallet Creation
1. **startDate** is required
2. **termMonths** must be ≥ 1
3. **interestRate** must be > 0
4. **deadline** is automatically calculated = startDate + termMonths

### Business Logic
1. Only ACTIVE SAVINGS wallets are processed
2. Processing only occurs on or after deadline
3. Each wallet is processed only once
4. User must have a DEFAULT wallet to receive matured funds

## Automatic Processing Flow

### 1. Daily Scheduler
```java
@Scheduled(cron = "0 0 0 * * ?") // Midnight daily
public void processExpiredSavingsWallets()
```

### 2. Processing Steps
1. **Find Expired Wallets**: Query SAVINGS wallets where `deadline <= current_date` and `status = ACTIVE`
2. **Calculate Maturity**: Apply interest formula
3. **Update Status**: Set SAVINGS wallet status to `DONE`
4. **Transfer Funds**: Add maturity amount to user's DEFAULT wallet
5. **Create Transaction**: Record transfer with description "Saving from {wallet_name}"
6. **Set Category**: Use "savings" category

### 3. Transaction Details
```java
Transaction {
    wallet: DEFAULT_WALLET,
    amount: MATURITY_AMOUNT,
    description: "Saving from Tiết kiệm 6 tháng",
    category: "savings",
    transactionTime: CURRENT_TIMESTAMP
}
```

## Error Handling

### 1. Missing DEFAULT Wallet
- **Error**: User has no DEFAULT wallet
- **Action**: Log error, skip processing
- **Resolution**: User must create DEFAULT wallet

### 2. Calculation Errors
- **Error**: Invalid interest rate or term
- **Action**: Use principal amount only
- **Log**: Warning with details

### 3. Database Errors
- **Error**: Save/update failures
- **Action**: Continue with next wallet
- **Log**: Error with full stack trace

## Monitoring & Logging

### 1. Daily Processing Logs
```
INFO: Starting daily check for expired SAVINGS wallets
INFO: Found 3 expired SAVINGS wallets to process
INFO: Processing expired SAVINGS wallet ID 123 for user ID 456
INFO: SAVINGS wallet ID 123 - Principal: 10000000, Rate: 8.5%, Term: 6 months, Maturity: 10425000
INFO: Successfully processed SAVINGS wallet ID 123 - transferred 10425000 to DEFAULT wallet
INFO: Completed processing expired SAVINGS wallets
```

### 2. Error Logs
```
ERROR: No DEFAULT wallet found for user ID 456 - cannot transfer matured savings
ERROR: Error processing expired SAVINGS wallet ID 123: Division by zero
```

## Usage Examples

### 1. Create SAVINGS Wallet
```bash
POST /api/v1/wallet
{
  "type": "SAVINGS",
  "name": "Tiết kiệm Tết 2026",
  "targetAmount": 50000000,
  "interestRate": 7.2,
  "startDate": "2025-07-17",
  "termMonths": 12
}
```

### 2. Check Wallet Status
```bash
GET /api/v1/wallet
# Response includes status field showing ACTIVE/DONE
```

### 3. Manual Processing (Admin)
```bash
POST /api/v1/wallet/process-expired-savings
# Headers: Authorization: Bearer <admin_token>
```

### 4. View Maturity Transactions
```bash
GET /api/v1/transaction/wallet/{defaultWalletId}
# Look for transactions with category "savings"
```

## Migration Required

### Database Schema Updates
```sql
-- Add new columns to wallets table
ALTER TABLE wallets ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE';
ALTER TABLE wallets ADD COLUMN start_date DATE;
ALTER TABLE wallets ADD COLUMN term_months INTEGER;

-- Update existing wallets to have ACTIVE status
UPDATE wallets SET status = 'ACTIVE' WHERE status IS NULL;
```

### Data Migration
- Existing wallets will have `status = ACTIVE`
- SAVINGS wallets without `startDate` won't be processed
- No impact on existing functionality

## Testing

### 1. Unit Tests
- Interest calculation accuracy
- Validation rules enforcement
- Error handling scenarios

### 2. Integration Tests
- End-to-end wallet creation
- Scheduled processing simulation
- Transaction creation verification

### 3. Manual Testing
1. Create SAVINGS wallet with future deadline
2. Use admin endpoint to trigger processing
3. Verify no change (deadline not reached)
4. Update deadline to past date in database
5. Trigger processing again
6. Verify status change and fund transfer

## Security Considerations

### 1. Admin-only Processing
- Manual processing requires ADMIN role
- Prevents unauthorized triggering

### 2. Validation
- All input validation on wallet creation
- Prevents negative interest rates
- Ensures valid date ranges

### 3. Audit Trail
- All transactions are logged
- Comprehensive error logging
- Processing history in logs

## Future Enhancements

### 1. Compound Interest
- Support for compound interest calculation
- Configurable compounding frequency

### 2. Early Withdrawal
- Allow early withdrawal with penalty
- Configurable penalty rates

### 3. Auto-renewal
- Option to automatically renew SAVINGS wallets
- Email notifications on maturity

### 4. Flexible Scheduling
- Custom maturity dates
- Weekend/holiday handling

Tính năng này cung cấp một hệ thống gửi tiết kiệm hoàn chỉnh với tự động hóa cao và khả năng audit tốt.
