package com.exe201.project.repository;

import com.exe201.project.entity.User;
import com.exe201.project.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    List<User> findAllByStatus(UserStatus status);
    List<User> findByStreakDaysGreaterThanAndLastCheckInDateLessThan(Integer streakDays, LocalDate date);
}