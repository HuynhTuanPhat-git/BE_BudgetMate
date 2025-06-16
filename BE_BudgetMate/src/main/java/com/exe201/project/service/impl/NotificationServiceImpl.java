package com.exe201.project.service.impl;

import com.exe201.project.dto.request.notification.CreateNotificationRequest;
import com.exe201.project.dto.response.notification.NotificationResponse;
import com.exe201.project.entity.Notification;
import com.exe201.project.entity.User;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.mapper.NotificationMapper;
import com.exe201.project.repository.NotificationRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Notification notification = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .dataPayload(request.getDataPayload())
                .isRead(false)
                .isActive(true)
                .build();
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Created notification with ID {} for user {}", savedNotification.getId(), user.getId());
        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getActiveNotificationsForUser(Long userId, Boolean unreadOnly, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Page<Notification> notificationsPage;
        if (Boolean.TRUE.equals(unreadOnly)) {
            notificationsPage = notificationRepository.findByUserAndIsActiveTrueAndIsReadFalseOrderByCreatedAtDesc(user, pageable);
        } else {
            notificationsPage = notificationRepository.findByUserAndIsActiveTrueOrderByCreatedAtDesc(user, pageable);
        }
        return notificationsPage.map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveUnreadNotificationCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return notificationRepository.countByUserAndIsActiveTrueAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public boolean markNotificationAsRead(Long userId, UUID notificationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Notification notification = notificationRepository.findByIdAndUser(notificationId, user)
                .orElse(null);

        if (notification != null && !notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("Marked notification {} as read for user {}", notificationId, userId);
            return true;
        }
        log.warn("Failed to mark notification {} as read for user {} (not found, already read, or belongs to another user)", notificationId, userId);
        return false;
    }

    @Override
    @Transactional
    public int markAllActiveNotificationsAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        int count = notificationRepository.markAllActiveAsReadForUser(user);
        log.info("Marked {} active notifications as read for user {}", count, userId);
        return count;
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long userId, UUID notificationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        int updatedCount = notificationRepository.deleteByIdAndUser(notificationId, user);
        if (updatedCount > 0) {
            log.info("Deleted notification {} for user {}", notificationId, userId);
            return true;
        }
        log.warn("Failed to delete notification {} for user {} (not found or already inactive)", notificationId, userId);
        return false;
    }
}
