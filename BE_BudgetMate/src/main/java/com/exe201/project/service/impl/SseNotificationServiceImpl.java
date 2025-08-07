package com.exe201.project.service.impl;

import com.exe201.project.dto.event.NotificationEvent;
import com.exe201.project.dto.response.notification.NotificationResponse;
import com.exe201.project.service.ISseNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.springframework.beans.factory.DisposableBean;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SseNotificationServiceImpl implements ISseNotificationService, DisposableBean {

    private final ObjectMapper objectMapper;

    // Map để lưu trữ SSE connections của từng user
    private final ConcurrentHashMap<Long, SseEmitter> userConnections = new ConcurrentHashMap<>();

    // Timeout cho SSE connection (30 phút)
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    // Heartbeat scheduler để keep connection alive
    private final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);

    public SseNotificationServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // Gửi heartbeat mỗi 25 giây để keep connection alive
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeat, 25, 25, TimeUnit.SECONDS);
    }

    @Override
    public SseEmitter createConnection(Long userId) {
        // Đóng connection cũ nếu có
        closeConnection(userId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Setup event handlers
        emitter.onCompletion(() -> {
            userConnections.remove(userId);
            log.info("SSE connection completed for user {}", userId);
        });

        emitter.onTimeout(() -> {
            userConnections.remove(userId);
            log.info("SSE connection timed out for user {}", userId);
        });

        emitter.onError((throwable) -> {
            userConnections.remove(userId);
            log.error("SSE connection error for user {}: {}", userId, throwable.getMessage());
        });

        // Lưu connection
        userConnections.put(userId, emitter);

        // Gửi welcome message
        try {
            NotificationEvent welcomeEvent = new NotificationEvent(
                    NotificationEvent.CONNECTION_ESTABLISHED,
                    null
            );

            emitter.send(SseEmitter.event()
                    .name("connection")
                    .data(objectMapper.writeValueAsString(welcomeEvent))
                    .id(welcomeEvent.getEventId()));

            log.info("SSE connection established for user {}", userId);

        } catch (IOException e) {
            log.error("Error sending welcome message to user {}: {}", userId, e.getMessage());
            userConnections.remove(userId);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @Override
    public void sendNotificationToUser(Long userId, NotificationResponse notification) {
        SseEmitter emitter = userConnections.get(userId);

        if (emitter == null) {
            log.debug("No SSE connection found for user {}", userId);
            return;
        }

        try {
            NotificationEvent event = new NotificationEvent(
                    NotificationEvent.NEW_NOTIFICATION,
                    notification
            );

            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(objectMapper.writeValueAsString(event))
                    .id(event.getEventId()));

            log.info("Sent SSE notification to user {}: {}", userId, notification.getTitle());

        } catch (IOException e) {
            log.error("Error sending SSE notification to user {}: {}", userId, e.getMessage());
            closeConnection(userId);
        }
    }

    @Override
    public void sendNotificationToAll(NotificationResponse notification) {
        if (userConnections.isEmpty()) {
            log.debug("No active SSE connections to send notification");
            return;
        }

        log.info("Sending SSE notification to {} online users", userConnections.size());

        userConnections.keySet().forEach(userId -> {
            sendNotificationToUser(userId, notification);
        });
    }

    @Override
    public void closeConnection(Long userId) {
        SseEmitter emitter = userConnections.remove(userId);
        if (emitter != null) {
            try {
                emitter.complete();
                log.info("Closed SSE connection for user {}", userId);
            } catch (Exception e) {
                log.error("Error closing SSE connection for user {}: {}", userId, e.getMessage());
            }
        }
    }

    @Override
    public boolean isUserOnline(Long userId) {
        return userConnections.containsKey(userId);
    }

    @Override
    public int getOnlineUserCount() {
        return userConnections.size();
    }

    /**
     * Gửi heartbeat để keep connections alive
     */
    private void sendHeartbeat() {
        if (userConnections.isEmpty()) {
            return;
        }

        log.debug("Sending heartbeat to {} connections", userConnections.size());

        NotificationEvent heartbeatEvent = new NotificationEvent(
                NotificationEvent.HEARTBEAT,
                null
        );

        userConnections.entrySet().removeIf(entry -> {
            Long userId = entry.getKey();
            SseEmitter emitter = entry.getValue();

            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data(objectMapper.writeValueAsString(heartbeatEvent))
                        .id(heartbeatEvent.getEventId()));
                return false; // Keep connection
            } catch (IOException e) {
                log.warn("Heartbeat failed for user {}, removing connection: {}", userId, e.getMessage());
                try {
                    emitter.complete();
                } catch (Exception ex) {
                    log.error("Error completing emitter for user {}: {}", userId, ex.getMessage());
                }
                return true; // Remove connection
            }
        });
    }

    /**
     * Shutdown scheduler khi service bị destroy
     * Implementation của DisposableBean interface
     */
    @Override
    public void destroy() {
        heartbeatScheduler.shutdown();
        userConnections.values().forEach(emitter -> {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.error("Error completing emitter during shutdown: {}", e.getMessage());
            }
        });
        userConnections.clear();
    }
}