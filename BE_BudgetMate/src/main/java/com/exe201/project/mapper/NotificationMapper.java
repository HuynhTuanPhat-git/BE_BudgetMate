package com.exe201.project.mapper;

import com.exe201.project.dto.response.notification.NotificationResponse;
import com.exe201.project.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .dataPayload(notification.getDataPayload())
                .isRead(notification.isRead())
                .isActive(notification.isActive())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
