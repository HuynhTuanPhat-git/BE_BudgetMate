package com.exe201.project.service.impl;

import com.exe201.project.dto.response.streak.CheckInResponse;
import com.exe201.project.entity.User;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.IStreakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreakServiceImpl implements IStreakService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public CheckInResponse performCheckIn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        LocalDate today = LocalDate.now();
        int currentStreak = (user.getStreakDays() == null) ? 0 : user.getStreakDays();
        LocalDate lastCheckIn = user.getLastCheckInDate();
        int creditsAwardedToday = 0;
        int totalUserCredits = (user.getCredits() == null) ? 0 : user.getCredits();

        String message;

        if (lastCheckIn != null && lastCheckIn.isEqual(today)) {
            message = "You have already checked in today. Your current streak is " + currentStreak + ".";
        } else {
            if (lastCheckIn == null || !lastCheckIn.isEqual(today.minusDays(1))) {
                currentStreak = 1;
                message = "Welcome back! Your new streak starts today at 1 day.";
            } else {
                currentStreak++;
                message = "Great job! Your streak is now " + currentStreak + " days.";
            }

            creditsAwardedToday = calculateCreditsForStreak(currentStreak);
            user.setStreakDays(currentStreak);
            user.setLastCheckInDate(today);
            totalUserCredits += creditsAwardedToday;
            user.setCredits(totalUserCredits);

            if (creditsAwardedToday > 0) {
                message += " You've earned " + creditsAwardedToday + " credit(s)!";
            }
            userRepository.save(user);
            log.info("User {} checked in. New streak: {}, Credits awarded: {}, Total credits: {}",
                    userId, currentStreak, creditsAwardedToday, user.getCredits());
        }

        return CheckInResponse.builder()
                .currentStreak(user.getStreakDays())
                .creditsAwarded(creditsAwardedToday)
                .totalCredits(user.getCredits())
                .message(message)
                .build();
    }

    private int calculateCreditsForStreak(int streak) {
        if (streak >= 30) {
            return 4;
        } else if (streak >= 14) {
            return 3;
        } else if (streak >= 7) {
            return 2;
        } else if (streak >= 4) {
            return 1;
        } else {
            return 0;
        }
    }
}