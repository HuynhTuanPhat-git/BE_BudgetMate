package com.exe201.project.dto.response.quiz;

import lombok.Builder;

@Builder
public record DailyQuizStatusResponse(
        Integer completedQuizzes,
        Integer remainingQuizzes,
        Integer dailyLimit,
        Boolean canTakeQuiz,
        Integer creditsEarnedToday
) {
}