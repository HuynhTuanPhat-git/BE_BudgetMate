package com.exe201.project.dto.request.notification;

import com.exe201.project.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateNotificationRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Message cannot be blank")
    private String message;

    @NotNull(message = "Notification type cannot be null")
    private NotificationType type;

    private String dataPayload;
}
