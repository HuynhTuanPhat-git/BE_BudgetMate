package com.exe201.project.controller;

import com.exe201.project.configuration.security.jwt.UserDetailsImpl;
import com.exe201.project.dto.request.question.SubmitQuizRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.question.QuestionResponse;
import com.exe201.project.dto.response.quiz.DailyQuizStatusResponse;
import com.exe201.project.dto.response.quiz.QuizResultResponse;
import com.exe201.project.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/quizzes")
@RequiredArgsConstructor
@Validated
@Slf4j
@CrossOrigin("*")
@Tag(name = "Quiz System", description = "User quiz APIs for daily quiz challenges")
@SecurityRequirement(name = "bearerAuth")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/submit")
    @Operation(
            summary = "Submit quiz answer",
            description = "Submit an answer for a quiz question. Earn credits for correct answers."
    )
    public ResponseEntity<ApiResponse<QuizResultResponse>> submitQuizAnswer(
            Authentication authentication,
            @Valid @RequestBody SubmitQuizRequest request) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        QuizResultResponse result = quizService.submitQuizAnswer(userId, request);

        String message = result.isCorrect() ?
                "Correct answer! Well done!" :
                "Incorrect answer. The correct answer is: " + result.correctAnswerText();

        return ResponseEntity.ok(ApiResponse.<QuizResultResponse>builder()
                .message(message)
                .data(result)
                .build());
    }

    @GetMapping("/daily")
    @Operation(
            summary = "Get daily quiz questions",
            description = "Get up to 3 random quiz questions for the current day. Each user can only take 3 quizzes per day."
    )
    public ResponseEntity<ApiResponse<List<QuestionResponse>>> getDailyQuizQuestions(
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        List<QuestionResponse> questions = quizService.getDailyQuizQuestions(userId);

        return ResponseEntity.ok(ApiResponse.<List<QuestionResponse>>builder()
                .message("Daily quiz questions retrieved successfully")
                .data(questions)
                .build());
    }

    @GetMapping("/status")
    @Operation(
            summary = "Get daily quiz status",
            description = "Check how many quizzes completed today and remaining quota"
    )
    public ResponseEntity<ApiResponse<DailyQuizStatusResponse>> getDailyQuizStatus(
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        DailyQuizStatusResponse status = quizService.getDailyQuizStatus(userId);

        return ResponseEntity.ok(ApiResponse.<DailyQuizStatusResponse>builder()
                .message("Daily quiz status retrieved successfully")
                .data(status)
                .build());
    }
}