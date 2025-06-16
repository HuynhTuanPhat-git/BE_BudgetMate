package com.exe201.project.controller;

import com.exe201.project.dto.request.question.CreateQuestionRequest;
import com.exe201.project.dto.request.question.QuestionSearchRequest;
import com.exe201.project.dto.request.question.UpdateQuestionRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.PagedResponse;
import com.exe201.project.dto.response.question.QuestionResponse;
import com.exe201.project.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/questions")
@RequiredArgsConstructor
@Validated
@Slf4j
@CrossOrigin("*")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Question Management", description = "Admin APIs for managing quiz questions")
@SecurityRequirement(name = "bearerAuth")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @Operation(
            summary = "Create new question",
            description = "Create a new quiz question with multiple choice answers (Admin only)"
    )
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request) {
        QuestionResponse response = questionService.createQuestion(request);
        return ResponseEntity.ok(ApiResponse.<QuestionResponse>builder()
                .message("Question created successfully")
                .data(response)
                .build());
    }

    @GetMapping
    @Operation(
            summary = "Get questions",
            description = "Search and filter questions with pagination (Admin only)."
    )
    public ResponseEntity<ApiResponse<PagedResponse<QuestionResponse>>> getQuestions(
            @Parameter(description = "Search keyword")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {

        QuestionSearchRequest request = QuestionSearchRequest.builder()
                .keyword(keyword)
                .isActive(true)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        Page<QuestionResponse> response = questionService.getQuestions(request);
        PagedResponse<QuestionResponse> pagedResponse = new PagedResponse<>(response);

        return ResponseEntity.ok(ApiResponse.<PagedResponse<QuestionResponse>>builder()
                .message("Questions retrieved successfully")
                .data(pagedResponse)
                .build());
    }

    @GetMapping("/{questionId}")
    @Operation(
            summary = "Get question by ID",
            description = "Retrieve a specific question with its answers (Admin only)"
    )
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestion(
            @Parameter(description = "Question ID", required = true)
            @PathVariable UUID questionId) {
        QuestionResponse response = questionService.getQuestionById(questionId);
        return ResponseEntity.ok(ApiResponse.<QuestionResponse>builder()
                .message("Question retrieved successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{questionId}")
    @Operation(
            summary = "Update question",
            description = "Update an existing quiz question (Admin only)"
    )
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @Parameter(description = "Question ID", required = true)
            @PathVariable UUID questionId,
            @Valid @RequestBody UpdateQuestionRequest request) {
        QuestionResponse response = questionService.updateQuestion(questionId, request);
        return ResponseEntity.ok(ApiResponse.<QuestionResponse>builder()
                .message("Question updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{questionId}")
    @Operation(
            summary = "Delete question",
            description = "Delete a quiz question by setting it inactive (Admin only)"
    )
    public ResponseEntity<ApiResponse<String>> deleteQuestion(
            @Parameter(description = "Question ID", required = true)
            @PathVariable UUID questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .message("Question deleted successfully")
                .data("Question has been deactivated")
                .build());
    }
}