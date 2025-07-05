package com.exe201.project.scheduler;

import com.exe201.project.dto.request.notification.CreateNotificationRequest;
import com.exe201.project.entity.User;
import com.exe201.project.enums.NotificationType;
import com.exe201.project.enums.UserStatus;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.INotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final INotificationService notificationService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 7 * * ?")
    public void sendDailyQuizReminders() {
        log.info("Starting job: Send Daily Quiz Reminders");

        List<User> activeUsers = userRepository.findAllByStatus(UserStatus.ACTIVE);

        if (activeUsers.isEmpty()) {
            log.info("No active users found to send daily quiz reminders.");
            return;
        }

        String title = "Daily Quiz Reminder!";
        String message = "Don't forget to complete your daily quiz and keep your streak going! \uD83D\uDCDD\uD83D\uDD25";

        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("screen", "QuizSection");
        payloadMap.put("action", "OPEN_DAILY_QUIZ");
        String dataPayloadJson = convertPayloadToJson(payloadMap, "daily quiz reminder");

        int successCount = 0;
        for (User user : activeUsers) {
            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .userId(user.getId())
                    .title(title)
                    .message(message)
                    .type(NotificationType.REMINDER)
                    .dataPayload(dataPayloadJson)
                    .build();
            try {
                notificationService.createNotification(request);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to create daily quiz reminder for user {}: {}", user.getId(), e.getMessage());
            }
        }
        log.info("Finished job: Send Daily Quiz Reminders. Sent {} notifications.", successCount);
    }

    @Scheduled(cron = "0 0 20 * * ?")
    // @Scheduled(cron = "0 */2 * * * ?") // Để test: chạy mỗi 2 phút
    public void sendStreakReminderNotifications() {
        log.info("Starting job: Send Streak Reminder Notifications");
        LocalDate today = LocalDate.now();
        List<User> activeUsers = userRepository.findAllByStatus(UserStatus.ACTIVE);

        if (activeUsers.isEmpty()) {
            log.info("No active users found to send streak reminders.");
            return;
        }

        String title = "Don't lose your streak! \uD83D\uDD25";
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("screen", "StreakCheckInSection");
        payloadMap.put("action", "CHECK_IN_NOW");
        String dataPayloadJson = convertPayloadToJson(payloadMap, "streak reminder");

        int successCount = 0;
        for (User user : activeUsers) {
            boolean hasCheckedInToday = user.getLastCheckInDate() != null && user.getLastCheckInDate().isEqual(today);
            int currentStreak = user.getStreakDays() == null ? 0 : user.getStreakDays();

            if (!hasCheckedInToday) {
                String message;
                if (currentStreak > 0) {
                    message = "Remember to check in today to keep your awesome streak of " + currentStreak + " days going!";
                } else {
                    message = "Don't forget to check in today and start a new streak!";
                }

                CreateNotificationRequest request = CreateNotificationRequest.builder()
                        .userId(user.getId())
                        .title(title)
                        .message(message)
                        .type(NotificationType.REMINDER)
                        .dataPayload(dataPayloadJson)
                        .build();
                try {
                    notificationService.createNotification(request);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to create streak reminder notification for user {}: {}", user.getId(), e.getMessage());
                }
            }
        }
        log.info("Finished job: Send Streak Reminder Notifications. Sent {} notifications.", successCount);
    }

    private String convertPayloadToJson(Map<String, String> payloadMap, String context) {
        try {
            return objectMapper.writeValueAsString(payloadMap);
        } catch (JsonProcessingException e) {
            log.error("Error converting dataPayload to JSON string for {}: {}", context, e.getMessage());
            return null;
        }
    }
}