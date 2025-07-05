package com.exe201.project.mapper;

import com.exe201.project.dto.response.answer.AnswerResponse;
import com.exe201.project.dto.response.question.QuestionResponse;
import com.exe201.project.entity.Answer;
import com.exe201.project.entity.Question;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class QuestionMapper {

    public QuestionResponse toQuestionResponse(Question question) {
        return getQuestionResponse(question, false);
    }

    public AnswerResponse toAnswerResponse(Answer answer) {
        if (answer == null) {
            return null;
        }

        return AnswerResponse.builder()
                .id(answer.getId())
                .answerText(answer.getAnswerText())
                .displayOrder(answer.getDisplayOrder())
                .isCorrect(answer.getIsCorrect())
                .isActive(answer.getIsActive())
                .build();
    }

    // For quiz responses without correct answer information
    public QuestionResponse toQuizQuestionResponse(Question question) {
        return getQuestionResponse(question, true);
    }

    private QuestionResponse getQuestionResponse(Question question, boolean filterActiveAnswers) {
        if (question == null) {
            return null;
        }

        List<AnswerResponse> answerResponses = question.getAnswers() != null ?
                question.getAnswers().stream()
                        .filter(ans -> !filterActiveAnswers || Boolean.TRUE.equals(ans.getIsActive()))
                        .sorted(Comparator.comparingInt(Answer::getDisplayOrder))
                        .map(this::toAnswerResponse)
                        .toList() : List.of();

        return QuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .answers(answerResponses)
                .build();
    }
}
