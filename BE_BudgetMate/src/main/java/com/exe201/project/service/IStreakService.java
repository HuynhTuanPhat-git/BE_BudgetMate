package com.exe201.project.service;

import com.exe201.project.dto.response.streak.CheckInResponse;

public interface IStreakService {
    CheckInResponse performCheckIn(Long userId);
}
