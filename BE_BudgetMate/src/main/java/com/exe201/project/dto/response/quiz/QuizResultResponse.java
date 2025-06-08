package com.exe201.project.dto.response.quiz;

import lombok.Builder;

@Builder
public record QuizResultResponse(
        Boolean isCorrect,
        Integer pointsEarned,
        Integer totalCredits,
        String correctAnswerText
) {
}