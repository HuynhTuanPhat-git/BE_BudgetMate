package com.exe201.project.dto.response.notification;

import com.exe201.project.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String message;
    private NotificationType type;
    private String dataPayload;
    private boolean isRead;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
