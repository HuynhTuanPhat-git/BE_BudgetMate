package com.exe201.project.dto.response.answer;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AnswerResponse(
        UUID id,
        String answerText,
        Integer displayOrder,
        Boolean isActive
) {
}
