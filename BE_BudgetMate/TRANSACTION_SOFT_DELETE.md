# Transaction Management System - Soft Delete & Audit Trail

## Overview
Hệ thống quản lý giao dịch đã được cập nhật để sử dụng soft delete và lưu trữ audit trail khi cập nhật hoặc xóa giao dịch.

## Key Changes

### 1. Transaction Entity Updates
- **Soft Delete**: Thêm field `isDeleted` để đánh dấu giao dịch đã xóa thay vì xóa vĩnh viễn
- **Audit Trail**: Thêm các field để lưu thông tin audit:
  - `createdAt`: Thời gian tạo giao dịch
  - `updatedAt`: Thời gian cập nhật gần nhất
  - `originalAmount`: Số tiền gốc trước khi sửa
  - `originalDescription`: Mô tả gốc trước khi sửa
  - `originalTransactionTime`: Thời gian giao dịch gốc trước khi sửa

### 2. Updated Operations

#### Delete Transaction (Soft Delete)
- **Trước**: Tạo giao dịch ngược để bù trừ và xóa giao dịch gốc
- **Sau**: Đặt `isDeleted = true` và lưu thông tin gốc vào các field `original*`
- **Lợi ích**: 
  - Không tạo thêm giao dịch mới
  - Giữ lại hoàn toàn thông tin giao dịch gốc
  - Có thể khôi phục nếu cần

```java
// Old approach - tạo giao dịch ngược
Transaction deleteTransaction = new Transaction();
deleteTransaction.setAmount(-originalAmount);
// ...

// New approach - soft delete
transaction.setDeleted(true);
transaction.setOriginalAmount(transaction.getAmount());
// ...
```

#### Update Transaction (Direct Update with Audit)
- **Trước**: Tạo 2 giao dịch mới (xóa cũ + thêm mới)
- **Sau**: Cập nhật trực tiếp giao dịch cũ và lưu giá trị gốc
- **Lợi ích**:
  - Không tạo thêm giao dịch
  - Lưu trữ hoàn chỉnh lịch sử thay đổi
  - Dễ dàng theo dõi thay đổi

```java
// Old approach - tạo 2 giao dịch mới
Transaction deleteTransaction = new Transaction(); // Giao dịch xóa
Transaction addTransaction = new Transaction();    // Giao dịch thêm

// New approach - cập nhật trực tiếp
transaction.setOriginalAmount(transaction.getAmount());
transaction.setAmount(newAmount);
// ...
```

### 3. Repository Updates
Tất cả các query đã được cập nhật để lọc bỏ các giao dịch đã xóa:

```java
// Old
List<Transaction> findByWalletIdOrderByTransactionTimeDesc(Long walletId);

// New  
List<Transaction> findByWalletIdAndIsDeletedFalseOrderByTransactionTimeDesc(Long walletId);
```

### 4. New API Endpoints

#### Get Deleted Transactions (Audit Trail)
```
GET /api/v1/transaction/wallet/{walletId}/deleted
```
- Trả về danh sách các giao dịch đã bị xóa
- Bao gồm thông tin gốc trước khi xóa
- Sắp xếp theo thời gian cập nhật (mới nhất trước)

### 5. Enhanced Response Data
`TransactionResponse` giờ bao gồm thêm thông tin audit:

```json
{
  "id": 1,
  "amount": 100000,
  "description": "Mua sắm",
  "transactionTime": "2025-07-15T10:00:00",
  "walletId": 1,
  "walletName": "Default Wallet",
  "categoryId": 1,
  "categoryName": "Shopping",
  "isDeleted": false,
  "createdAt": "2025-07-15T09:00:00",
  "updatedAt": "2025-07-15T11:00:00",
  "originalAmount": 50000,           // null nếu chưa được sửa
  "originalDescription": "Mua đồ ăn", // null nếu chưa được sửa
  "originalTransactionTime": "2025-07-15T09:30:00" // null nếu chưa được sửa
}
```

## Benefits

### 1. Data Integrity
- Không mất dữ liệu khi xóa hoặc sửa
- Có thể khôi phục giao dịch đã xóa
- Theo dõi được lịch sử thay đổi

### 2. Audit Trail
- Xem được giá trị gốc trước khi sửa
- Biết được thời gian tạo và cập nhật
- Có thể theo dõi hành vi người dùng

### 3. Performance
- Không tạo thêm giao dịch không cần thiết
- Giảm số lượng record trong database
- Query nhanh hơn

### 4. User Experience
- Có thể xem lại giao dịch đã xóa
- Hiểu được lịch sử thay đổi
- Có thể khôi phục nếu xóa nhầm

## Migration Notes

### Database Changes Required
Khi deploy lên production, cần chạy migration để thêm các column mới:

```sql
ALTER TABLE transaction ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE transaction ADD COLUMN created_at TIMESTAMP;
ALTER TABLE transaction ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE transaction ADD COLUMN original_amount DOUBLE;
ALTER TABLE transaction ADD COLUMN original_description VARCHAR(255);
ALTER TABLE transaction ADD COLUMN original_transaction_time TIMESTAMP;
```

### Backward Compatibility
- Các giao dịch cũ sẽ có `isDeleted = false`
- Các field audit sẽ là `null` cho dữ liệu cũ
- Không ảnh hưởng đến logic hiện tại

## Usage Examples

### 1. View Active Transactions
```
GET /api/v1/transaction/wallet/1
```
Chỉ trả về các giao dịch chưa bị xóa

### 2. View Deleted Transactions
```
GET /api/v1/transaction/wallet/1/deleted
```
Trả về các giao dịch đã bị xóa với thông tin gốc

### 3. Update Transaction
```
PUT /api/v1/transaction/123
{
  "amount": 200000,
  "description": "Mua sắm cập nhật"
}
```
- Cập nhật trực tiếp giao dịch
- Lưu giá trị cũ vào `original*` fields
- Cập nhật `updatedAt`

### 4. Delete Transaction
```
DELETE /api/v1/transaction/123
```
- Đặt `isDeleted = true`
- Lưu giá trị hiện tại vào `original*` fields
- Trừ tiền từ ví
- Cập nhật `updatedAt`

## Error Handling

### 1. Delete Already Deleted Transaction
```json
{
  "success": false,
  "message": "Transaction is already deleted",
  "errorCode": 404
}
```

### 2. Update Deleted Transaction
```json
{
  "success": false,
  "message": "Cannot update deleted transaction",
  "errorCode": 404
}
```

Hệ thống mới này cung cấp tính minh bạch cao hơn, khả năng audit tốt hơn và trải nghiệm người dùng được cải thiện while maintaining data integrity.
