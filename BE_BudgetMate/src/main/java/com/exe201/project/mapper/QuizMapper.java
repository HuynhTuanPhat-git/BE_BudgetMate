package com.exe201.project.mapper;

import com.exe201.project.dto.response.quiz.DailyQuizStatusResponse;
import com.exe201.project.dto.response.quiz.QuizResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuizMapper {

    public DailyQuizStatusResponse toDailyQuizStatusResponse(
            Long completedQuizzes,
            Integer dailyLimit,
            Integer creditsEarnedToday) {

        return DailyQuizStatusResponse.builder()
                .completedQuizzes(Math.toIntExact(completedQuizzes))
                .remainingQuizzes(Math.max(0, dailyLimit - Math.toIntExact(completedQuizzes)))
                .dailyLimit(dailyLimit)
                .canTakeQuiz(completedQuizzes < dailyLimit)
                .creditsEarnedToday(creditsEarnedToday != null ? creditsEarnedToday : 0)
                .build();
    }

    public QuizResultResponse toQuizResultResponse(
            Boolean isCorrect,
            Integer creditsEarned,
            Integer totalCredits,
            String correctAnswerText) {

        return QuizResultResponse.builder()
                .isCorrect(isCorrect)
                .pointsEarned(creditsEarned)
                .totalCredits(totalCredits)
                .correctAnswerText(correctAnswerText)
                .build();
    }
}
