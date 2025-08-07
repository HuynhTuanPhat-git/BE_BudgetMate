package com.exe201.project.service;

import com.exe201.project.dto.response.notification.NotificationResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ISseNotificationService {
    
    /**
     * Tạo SSE connection cho user
     */
    SseEmitter createConnection(Long userId);
    
    /**
     * Gửi notification qua SSE cho user cụ thể
     */
    void sendNotificationToUser(Long userId, NotificationResponse notification);
    
    /**
     * Gửi notification qua SSE cho tất cả user đang online
     */
    void sendNotificationToAll(NotificationResponse notification);
    
    /**
     * Đóng connection cho user
     */
    void closeConnection(Long userId);
    
    /**
     * Kiểm tra user có đang online không
     */
    boolean isUserOnline(Long userId);
    
    /**
     * Lấy số lượng user đang online
     */
    int getOnlineUserCount();
}
