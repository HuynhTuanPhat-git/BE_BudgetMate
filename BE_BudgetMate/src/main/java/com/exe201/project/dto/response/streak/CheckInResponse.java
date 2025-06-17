package com.exe201.project.dto.response.streak;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckInResponse {
    private int currentStreak;
    private int creditsAwarded;
    private int totalCredits;
    private String message;
}