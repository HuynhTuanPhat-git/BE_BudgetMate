package com.exe201.project.service;

import com.exe201.project.dto.request.question.CreateQuestionRequest;
import com.exe201.project.dto.request.question.QuestionSearchRequest;
import com.exe201.project.dto.request.question.UpdateQuestionRequest;
import com.exe201.project.dto.response.question.QuestionResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface QuestionService {
    QuestionResponse createQuestion(CreateQuestionRequest request);

    QuestionResponse updateQuestion(UUID questionId, UpdateQuestionRequest request);

    void deleteQuestion(UUID questionId);

    QuestionResponse getQuestionById(UUID questionId);

    Page<QuestionResponse> getQuestions(QuestionSearchRequest request);
}
