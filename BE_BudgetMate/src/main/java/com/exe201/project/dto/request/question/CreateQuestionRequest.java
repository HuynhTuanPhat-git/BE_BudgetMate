package com.exe201.project.dto.request.question;

import com.exe201.project.dto.request.answer.CreateAnswerRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateQuestionRequest(
        @NotBlank(message = "Question text is required")
        @Size(max = 1000, message = "Question text must not exceed 1000 characters")
        String questionText,

        @NotEmpty(message = "At least one answer is required")
        @Size(min = 2, max = 6, message = "Question must have between 2 and 6 answers")
        @Valid
        List<CreateAnswerRequest> answers
) {
}
