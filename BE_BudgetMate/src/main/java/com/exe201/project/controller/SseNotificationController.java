package com.exe201.project.controller;

import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.service.ISseNotificationService;
import com.exe201.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications/sse")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SSE Notifications", description = "Server-Sent Events for real-time notifications")
public class SseNotificationController {

    private final ISseNotificationService sseNotificationService;
    private final UserService userService;

    @Operation(summary = "Establish SSE connection for real-time notifications")
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('USER')")
    public SseEmitter connect() {
        Long userId = userService.getAuthenticatedUser().getId();
        log.info("User {} requesting SSE connection", userId);
        
        return sseNotificationService.createConnection(userId);
    }

    @Operation(summary = "Close SSE connection")
    @PostMapping("/disconnect")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> disconnect() {
        Long userId = userService.getAuthenticatedUser().getId();
        log.info("User {} requesting SSE disconnection", userId);
        
        sseNotificationService.closeConnection(userId);
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("SSE connection closed successfully")
                        .build()
        );
    }

    @Operation(summary = "Check if user is online")
    @GetMapping("/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Object>> getConnectionStatus() {
        Long userId = userService.getAuthenticatedUser().getId();
        boolean isOnline = sseNotificationService.isUserOnline(userId);
        
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("User online data")
                        .data(Map.of(
                                "userId", userId,
                                "isOnline", isOnline,
                                "totalOnlineUsers", sseNotificationService.getOnlineUserCount()
                        ))
                        .build()
        );
    }

    @Operation(summary = "Get online users count (Admin only)")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("User online count")
                        .data(Map.of(
                                "onlineUserCount", sseNotificationService.getOnlineUserCount(),
                                "timestamp", java.time.LocalDateTime.now()
                        ))
                        .build()
        );
    }
}
