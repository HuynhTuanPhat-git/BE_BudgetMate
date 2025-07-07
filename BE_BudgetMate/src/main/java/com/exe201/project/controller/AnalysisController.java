package com.exe201.project.controller;

import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.analysis.FinanceAnalysisResponse;
import com.exe201.project.entity.User;
import com.exe201.project.enums.AnalysisType;
import com.exe201.project.service.AnalysisService;
import com.exe201.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Tag(name = "Financial Analysis", description = "APIs for retrieving financial analysis data for the current user")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final UserService userService;

    @GetMapping("/finance")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get financial analysis for the current user",
            description = "Retrieves financial analysis data (income, expenses, debts, etc.) for the current user, based on the specified analysis type."
    )
    public ResponseEntity<ApiResponse<FinanceAnalysisResponse>> getFinanceAnalysis(
            @Parameter(
                    description = "Type of analysis to perform (MONTHLY, INSTANTLY)",
                    required = true,
                    schema = @Schema(implementation = AnalysisType.class)
            )
            @RequestParam AnalysisType type) {

        User user = userService.getAuthenticatedUser();
        FinanceAnalysisResponse analysis = analysisService.getProfileAnalysis(user.getId(), type);

        return ResponseEntity.ok(ApiResponse.<FinanceAnalysisResponse>builder()
                .data(analysis)
                .message("Finance analysis retrieved successfully")
                .build());
    }
}