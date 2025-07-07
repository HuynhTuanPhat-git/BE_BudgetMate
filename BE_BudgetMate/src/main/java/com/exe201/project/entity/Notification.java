package com.exe201.project.entity;

import com.exe201.project.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user_active_created", columnList = "user_id, is_active, created_at"),
        @Index(name = "idx_notification_user_active_read_created", columnList = "user_id, is_active, is_read, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    NotificationType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    String dataPayload;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    boolean isRead = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "read_at")
    LocalDateTime readAt;
}
