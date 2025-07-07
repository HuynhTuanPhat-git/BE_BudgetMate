package com.exe201.project.controller;

import com.exe201.project.configuration.security.utils.AuthenticationUtil;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.streak.CheckInResponse;
import com.exe201.project.service.IStreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/streaks")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Streak Management", description = "APIs for managing user daily check-in streaks")
@SecurityRequirement(name = "bearerAuth")
public class StreakController {

    private final IStreakService streakService;
    private final AuthenticationUtil authenticationUtil;

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
            summary = "Perform daily check-in",
            description = "Allows the authenticated user to perform their daily check-in, update streak, and earn credits."
    )
    public ResponseEntity<ApiResponse<CheckInResponse>> performCheckIn() {
        Long userId = authenticationUtil.getCurrentUserId();
        CheckInResponse response = streakService.performCheckIn(userId);
        return ResponseEntity.ok(ApiResponse.<CheckInResponse>builder()
                .message(response.getMessage())
                .data(response)
                .build());
    }
}