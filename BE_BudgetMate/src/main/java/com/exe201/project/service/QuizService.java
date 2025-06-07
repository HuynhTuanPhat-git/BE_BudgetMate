package com.exe201.project.service;

import com.exe201.project.dto.request.question.SubmitQuizRequest;
import com.exe201.project.dto.response.question.QuestionResponse;
import com.exe201.project.dto.response.quiz.DailyQuizStatusResponse;
import com.exe201.project.dto.response.quiz.QuizResultResponse;

import java.util.List;

public interface QuizService {
    List<QuestionResponse> getDailyQuizQuestions(Long userId);

    QuizResultResponse submitQuizAnswer(Long userId, SubmitQuizRequest request);

    DailyQuizStatusResponse getDailyQuizStatus(Long userId);
}