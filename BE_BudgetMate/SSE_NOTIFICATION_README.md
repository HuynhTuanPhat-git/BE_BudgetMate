# SSE Notification System

## Tổng quan
Hệ thống thông báo real-time sử dụng Server-Sent Events (SSE) cho phép gửi thông báo trực tiếp đến client mà không cần polling.

## Cấu trúc

### 1. Service Layer
- **ISseNotificationService**: Interface định nghĩa các phương thức SSE
- **SseNotificationServiceImpl**: Implementation quản lý SSE connections
- **NotificationServiceImpl**: Service chính được tích hợp SSE

### 2. Controller Layer
- **SseNotificationController**: Endpoints cho SSE connections

### 3. Event Model
- **NotificationEvent**: Object chứa thông tin event gửi qua SSE

## API Endpoints

### 1. Kết nối SSE
```
GET /api/notifications/sse/connect
Headers: Authorization: Bearer <token>
Content-Type: text/event-stream
```

### 2. Ngắt kết nối SSE
```
POST /api/notifications/sse/disconnect
Headers: Authorization: Bearer <token>
```

### 3. Kiểm tra trạng thái kết nối
```
GET /api/notifications/sse/status
Headers: Authorization: Bearer <token>
```

### 4. Thống kê (Admin only)
```
GET /api/notifications/sse/stats
Headers: Authorization: Bearer <token>
```

## Cách sử dụng từ Frontend

### JavaScript/TypeScript
```javascript
// Kết nối SSE
const eventSource = new EventSource('/api/notifications/sse/connect', {
    headers: {
        'Authorization': `Bearer ${token}`
    }
});

// Lắng nghe events
eventSource.addEventListener('notification', (event) => {
    const notificationEvent = JSON.parse(event.data);
    console.log('New notification:', notificationEvent.notification);
    
    // Hiển thị notification trong UI
    showNotification(notificationEvent.notification);
});

eventSource.addEventListener('connection', (event) => {
    const connectionEvent = JSON.parse(event.data);
    console.log('SSE connection established');
});

eventSource.addEventListener('heartbeat', (event) => {
    console.log('Heartbeat received');
});

// Xử lý errors
eventSource.onerror = (error) => {
    console.error('SSE connection error:', error);
};

// Đóng connection khi không cần
eventSource.close();
```

### React Hook
```javascript
import { useEffect, useState } from 'react';

export const useSSENotifications = (token) => {
    const [notifications, setNotifications] = useState([]);
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        if (!token) return;

        const eventSource = new EventSource('/api/notifications/sse/connect', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        eventSource.addEventListener('connection', () => {
            setIsConnected(true);
        });

        eventSource.addEventListener('notification', (event) => {
            const notificationEvent = JSON.parse(event.data);
            setNotifications(prev => [notificationEvent.notification, ...prev]);
        });

        eventSource.onerror = () => {
            setIsConnected(false);
        };

        return () => {
            eventSource.close();
            setIsConnected(false);
        };
    }, [token]);

    return { notifications, isConnected };
};
```

## Event Types

### 1. NEW_NOTIFICATION
Thông báo mới được tạo
```json
{
    "eventType": "NEW_NOTIFICATION",
    "notification": {
        "id": "uuid",
        "title": "Budget Alert",
        "message": "You have exceeded your daily spending limit",
        "type": "BUDGET_ALERT",
        "isRead": false,
        "createdAt": "2025-08-01T10:30:00"
    },
    "timestamp": "2025-08-01T10:30:00",
    "eventId": "event-uuid"
}
```

### 2. CONNECTION_ESTABLISHED
Kết nối SSE được thiết lập thành công
```json
{
    "eventType": "CONNECTION_ESTABLISHED",
    "notification": null,
    "timestamp": "2025-08-01T10:30:00",
    "eventId": "event-uuid"
}
```

### 3. HEARTBEAT
Keep-alive signal (gửi mỗi 25 giây)
```json
{
    "eventType": "HEARTBEAT",
    "notification": null,
    "timestamp": "2025-08-01T10:30:00",
    "eventId": "event-uuid"
}
```

## Features

### 1. Connection Management
- Automatic reconnection handling
- Connection timeout (30 minutes)
- Heartbeat để keep connection alive
- Graceful connection cleanup

### 2. User-specific Notifications
- Mỗi user có connection riêng biệt
- Notifications chỉ gửi đến user liên quan
- Track online/offline status

### 3. Error Handling
- Automatic connection cleanup on errors
- Fallback mechanisms
- Comprehensive logging

### 4. Security
- JWT authentication required
- User-specific connections
- CORS configuration

## Integration với Existing Code

### Trong TransactionServiceImpl
```java
// Tạo notification khi vượt quá budget
CreateNotificationRequest notificationRequest = CreateNotificationRequest.builder()
    .userId(user.getId())
    .title("Budget Alert")
    .message("You have exceeded your daily spending limit of " + defaultWallet.getTargetAmount())
    .type(NotificationType.BUDGET_ALERT)
    .build();

// Notification sẽ tự động được gửi qua SSE
notificationService.createNotification(notificationRequest);
```

## Testing

### 1. Manual Testing
- Sử dụng browser dev tools để test SSE connection
- Tạo notifications và kiểm tra real-time delivery

### 2. Automated Testing
- Unit tests cho SseNotificationServiceImpl
- Integration tests cho SSE endpoints

## Performance Considerations

### 1. Memory Management
- Automatic cleanup của dead connections
- Heartbeat để detect inactive connections
- Connection timeout để prevent memory leaks

### 2. Scalability
- Thread pool cho heartbeat scheduler
- Concurrent HashMap cho thread-safe connection storage
- Efficient event serialization

## Troubleshooting

### 1. Connection Issues
- Kiểm tra CORS configuration
- Verify JWT token validity
- Check network connectivity

### 2. Performance Issues
- Monitor số lượng active connections
- Check memory usage
- Review heartbeat frequency

### 3. Common Errors
- "Connection timeout": Increase SSE_TIMEOUT value
- "User not found": Verify authentication
- "CORS error": Check SseConfiguration settings
