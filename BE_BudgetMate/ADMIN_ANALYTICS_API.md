# Admin Analytics API Documentation

## Overview
Admin Analytics API cung cấp các endpoint cho admin để theo dõi doanh thu và thống kê giao dịch.

## Endpoints

### 1. Get Revenue Analytics
**GET** `/api/v1/admin/analytics/revenue`

Lấy thống kê doanh thu theo khoảng thời gian chỉ định.

**Parameters:**
- `period` (required): WEEKLY, MONTHLY, DAILY
- `startDate` (optional): Ngày bắt đầu (format: YYYY-MM-DD)
- `endDate` (optional): Ngày kết thúc (default: hôm nay)

**Response:**
```json
{
  "success": true,
  "message": "Revenue analytics retrieved successfully",
  "data": {
    "totalRevenue": 1500000.0,
    "startDate": "2025-07-21",
    "endDate": "2025-07-28",
    "period": "WEEKLY",
    "dailyBreakdown": [
      {
        "date": "2025-07-21",
        "revenue": 300000.0,
        "transactionCount": 5,
        "userCount": 4
      }
    ],
    "membershipBreakdown": [
      {
        "membershipPlanId": 1,
        "membershipName": "Premium",
        "revenue": 800000.0,
        "subscriptionCount": 8,
        "averageOrderValue": 100000.0
      }
    ]
  }
}
```

### 2. Get Weekly Revenue
**GET** `/api/v1/admin/analytics/revenue/weekly`

Lấy thống kê doanh thu tuần hiện tại.

### 3. Get Monthly Revenue
**GET** `/api/v1/admin/analytics/revenue/monthly`

Lấy thống kê doanh thu tháng hiện tại.

### 4. Get Membership Revenue Breakdown
**GET** `/api/v1/admin/analytics/revenue/membership`

Lấy thống kê doanh thu theo từng gói membership.

**Parameters:**
- `startDate` (required): Ngày bắt đầu
- `endDate` (required): Ngày kết thúc

**Response:**
```json
{
  "success": true,
  "message": "Membership revenue breakdown retrieved successfully",
  "data": [
    {
      "membershipPlanId": 1,
      "membershipName": "Premium",
      "revenue": 800000.0,
      "subscriptionCount": 8,
      "averageOrderValue": 100000.0
    },
    {
      "membershipPlanId": 2,
      "membershipName": "Basic",
      "revenue": 300000.0,
      "subscriptionCount": 10,
      "averageOrderValue": 30000.0
    }
  ]
}
```

### 5. Get Daily Transactions
**GET** `/api/v1/admin/analytics/transactions/daily`

Lấy thống kê giao dịch trong 1 ngày.

**Parameters:**
- `date` (optional): Ngày cần thống kê (default: hôm nay)

**Response:**
```json
{
  "success": true,
  "message": "Daily transaction statistics retrieved successfully",
  "data": {
    "date": "2025-07-28",
    "totalTransactions": 15,
    "successfulTransactions": 15,
    "failedTransactions": 0,
    "totalAmount": 500000.0,
    "uniqueUsers": 12
  }
}
```

## Authorization
Tất cả endpoints yêu cầu role ADMIN:
```
Authorization: Bearer <admin_jwt_token>
```

## Example Usage

### Lấy doanh thu tuần này:
```bash
curl -X GET "http://localhost:8080/api/v1/admin/analytics/revenue/weekly" \
  -H "Authorization: Bearer <admin_token>"
```

### Lấy doanh thu tháng này:
```bash
curl -X GET "http://localhost:8080/api/v1/admin/analytics/revenue/monthly" \
  -H "Authorization: Bearer <admin_token>"
```

### Lấy thống kê giao dịch hôm nay:
```bash
curl -X GET "http://localhost:8080/api/v1/admin/analytics/transactions/daily" \
  -H "Authorization: Bearer <admin_token>"
```

### Lấy doanh thu theo gói membership:
```bash
curl -X GET "http://localhost:8080/api/v1/admin/analytics/revenue/membership?startDate=2025-07-01&endDate=2025-07-31" \
  -H "Authorization: Bearer <admin_token>"
```
