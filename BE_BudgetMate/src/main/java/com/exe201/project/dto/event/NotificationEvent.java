package com.exe201.project.dto.event;

import com.exe201.project.dto.response.notification.NotificationResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    
    private String eventType;
    private NotificationResponse notification;
    private LocalDateTime timestamp;
    private String eventId;
    
    public NotificationEvent(String eventType, NotificationResponse notification) {
        this.eventType = eventType;
        this.notification = notification;
        this.timestamp = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
    
    // Event types constants
    public static final String NEW_NOTIFICATION = "NEW_NOTIFICATION";
    public static final String NOTIFICATION_READ = "NOTIFICATION_READ";
    public static final String NOTIFICATION_DELETED = "NOTIFICATION_DELETED";
    public static final String CONNECTION_ESTABLISHED = "CONNECTION_ESTABLISHED";
    public static final String HEARTBEAT = "HEARTBEAT";
}
