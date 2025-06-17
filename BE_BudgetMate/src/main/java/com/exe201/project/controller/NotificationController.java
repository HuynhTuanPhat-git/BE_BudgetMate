package com.exe201.project.controller;

import com.exe201.project.configuration.security.utils.AuthenticationUtil;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.PagedResponse;
import com.exe201.project.dto.response.notification.NotificationResponse;
import com.exe201.project.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs for managing user's notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final INotificationService notificationService;
    private final AuthenticationUtil authenticationUtil;

    @GetMapping
    @Operation(
            summary = "Get notifications for current user",
            description = "Retrieves a list of notifications for the authenticated user."
    )
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @Parameter(description = "Filter by unread status (true/false). If null, returns all active.")
            @RequestParam(required = false) Boolean unreadOnly,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Long userId = authenticationUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<NotificationResponse> notificationsPage = notificationService.getNotificationsForUser(userId, unreadOnly, pageable);
        PagedResponse<NotificationResponse> pagedResponse = new PagedResponse<>(notificationsPage);

        return ResponseEntity.ok(ApiResponse.<PagedResponse<NotificationResponse>>builder()
                .message("Notifications retrieved successfully.")
                .data(pagedResponse)
                .build());
    }

    @GetMapping("/unread-count")
    @Operation(
            summary = "Get unread notification count",
            description = "Retrieves the count of unread notifications for the authenticated user."
    )
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount() {
        Long userId = authenticationUtil.getCurrentUserId();
        long count = notificationService.getActiveUnreadNotificationCount(userId);
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .message("Active unread notification count retrieved successfully.")
                .data(count)
                .build());
    }

    @PutMapping("/{notificationId}/read")
    @Operation(
            summary = "Mark a notification as read",
            description = "Marks a specific notification as read for the authenticated user."
    )
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable UUID notificationId) {
        Long userId = authenticationUtil.getCurrentUserId();
        boolean success = notificationService.markNotificationAsRead(userId, notificationId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .message("Notification marked as read.")
                    .build());
        } else {
            return ResponseEntity.status(404).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Notification not found, already read, or access denied.")
                    .errorCode(404)
                    .build());
        }
    }

    @PutMapping("/read-all")
    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks all unread notifications as read for the authenticated user."
    )
    public ResponseEntity<ApiResponse<String>> markAllAsRead() {
        Long userId = authenticationUtil.getCurrentUserId();
        int count = notificationService.markAllNotificationsAsRead(userId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message(count + " active notifications marked as read.")
                .build());
    }

    @DeleteMapping("/{notificationId}")
    @Operation(
            summary = "Delete a notification",
            description = "Deletes a specific notification for the authenticated user."
    )
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable UUID notificationId) {
        Long userId = authenticationUtil.getCurrentUserId();
        boolean success = notificationService.deleteNotification(userId, notificationId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .message("Notification deleted successfully.")
                    .build());
        } else {
            return ResponseEntity.status(404).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Notification not found or already deleted.")
                    .errorCode(404)
                    .build());
        }
    }
}
