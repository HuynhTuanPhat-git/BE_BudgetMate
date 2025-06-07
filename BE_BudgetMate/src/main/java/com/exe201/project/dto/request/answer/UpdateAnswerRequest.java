package com.exe201.project.dto.request.answer;

import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateAnswerRequest(
        UUID id,

        @Size(max = 500, message = "Answer text must not exceed 500 characters")
        String answerText,

        Boolean isCorrect,

        Integer displayOrder,

        Boolean isActive
) {}