package com.exe201.project.dto.request.answer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateAnswerRequest(
        @NotBlank(message = "Answer text is required")
        @Size(max = 500, message = "Answer text must not exceed 500 characters")
        String answerText,

        @NotNull(message = "isCorrect field is required")
        Boolean isCorrect,

        Integer displayOrder
) {
}