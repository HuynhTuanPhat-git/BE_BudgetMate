package com.exe201.project.service;

import com.exe201.project.dto.request.notification.CreateNotificationRequest;
import com.exe201.project.dto.response.notification.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface INotificationService {
    NotificationResponse createNotification(CreateNotificationRequest request);
    Page<NotificationResponse> getNotificationsForUser(Long userId, Boolean unreadOnly, Pageable pageable);
    long getActiveUnreadNotificationCount(Long userId);
    boolean markNotificationAsRead(Long userId, UUID notificationId);
    int markAllNotificationsAsRead(Long userId);
    boolean deleteNotification(Long userId, UUID notificationId);
}
