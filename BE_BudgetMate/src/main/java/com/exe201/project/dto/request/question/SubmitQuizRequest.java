package com.exe201.project.dto.request.question;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SubmitQuizRequest(
        @NotNull(message = "Question ID is required")
        UUID questionId,

        @NotNull(message = "Answer ID is required")
        UUID answerId
) {
}