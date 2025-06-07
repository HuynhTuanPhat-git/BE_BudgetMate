package com.exe201.project.dto.response.question;

import com.exe201.project.dto.response.answer.AnswerResponse;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record QuestionResponse(
        UUID id,
        String questionText,
        List<AnswerResponse> answers
) {
}
