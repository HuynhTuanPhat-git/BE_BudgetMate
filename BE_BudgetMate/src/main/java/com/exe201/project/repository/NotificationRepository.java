package com.exe201.project.repository;

import com.exe201.project.entity.Notification;
import com.exe201.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Notification> findByUserAndIsActiveTrueAndIsReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByUserAndIsActiveTrueAndIsReadFalse(User user);

    Optional<Notification> findByIdAndUserAndIsActiveTrue(UUID notificationId, User user);

    Optional<Notification> findByIdAndUser(UUID notificationId, User user);

    @Modifying
    @Query("UPDATE Notification n " +
            "SET n.isRead = true, " +
            "n.readAt = CURRENT_TIMESTAMP " +
            "WHERE n.user = :user " +
            "AND n.isActive = true " +
            "AND n.isRead = false")
    int markAllActiveAsReadForUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE Notification n " +
            "SET n.isRead = true, " +
            "n.readAt = CURRENT_TIMESTAMP " +
            "WHERE n.id = :notificationId " +
            "AND n.user = :user " +
            "AND n.isActive = true")
    int markActiveAsReadByIdAndUser(@Param("notificationId") UUID notificationId, @Param("user") User user);

    @Modifying
    @Query("UPDATE Notification n " +
            "SET n.isActive = false " +
            "WHERE n.id = :notificationId " +
            "AND n.user = :user " +
            "AND n.isActive = true")
    int deleteByIdAndUser(@Param("notificationId") UUID notificationId, @Param("user") User user);
}
