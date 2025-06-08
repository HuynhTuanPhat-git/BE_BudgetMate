package com.exe201.project.dto.request.question;

import com.exe201.project.dto.request.answer.UpdateAnswerRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record UpdateQuestionRequest(
        @Size(max = 1000, message = "Question text must not exceed 1000 characters")
        String questionText,

        Boolean isActive,

        @Valid
        List<UpdateAnswerRequest> answers
) {
}