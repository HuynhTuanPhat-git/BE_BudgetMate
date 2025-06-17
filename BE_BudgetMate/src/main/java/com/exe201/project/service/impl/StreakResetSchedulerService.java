package com.exe201.project.service.impl;

import com.exe201.project.entity.User;
import com.exe201.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreakResetSchedulerService {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * ?")
//    @Scheduled(cron = "0 * * * * ?") // Để test: chạy mỗi phút
    @Transactional
    public void resetMissedStreaks() {
        log.info("Starting job: Reset Missed Streaks");
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<User> usersToReset = userRepository.findByStreakDaysGreaterThanAndLastCheckInDateLessThan(0, yesterday);

        if (usersToReset.isEmpty()) {
            log.info("Finished job: Reset Missed Streaks. No users needed resetting.");
            return;
        }

        int resetCount = 0;
        for (User user : usersToReset) {
            log.info("Resetting streak for user {}. Old streak: {}, Last check-in: {}",
                    user.getId(), user.getStreakDays(), user.getLastCheckInDate());
            user.setStreakDays(0);
            userRepository.save(user);
            resetCount++;
        }

        log.info("Finished job: Reset Missed Streaks. Reset {} users.", resetCount);
    }
}