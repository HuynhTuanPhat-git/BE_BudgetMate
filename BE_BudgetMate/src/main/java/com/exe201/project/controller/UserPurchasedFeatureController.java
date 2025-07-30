package com.exe201.project.controller;

import com.exe201.project.configuration.security.utils.AuthenticationUtil;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.user_purchased_feature.UserPurchasedFeatureResponse;
import com.exe201.project.service.UserPurchasedFeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/purchased-features")
@RequiredArgsConstructor
@Tag(name = "User Purchased Features", description = "APIs for managing user purchased features")
@SecurityRequirement(name = "bearerAuth")
public class UserPurchasedFeatureController {

    private final UserPurchasedFeatureService userPurchasedFeatureService;
    private final AuthenticationUtil authenticationUtil;

    @GetMapping
    @Operation(summary = "Get user purchased features",
            description = "Get all purchased features for the current user with remaining usage")
    public ResponseEntity<ApiResponse<List<UserPurchasedFeatureResponse>>> getUserPurchasedFeatures() {
        Long userId = authenticationUtil.getCurrentUserId();
        List<UserPurchasedFeatureResponse> response = userPurchasedFeatureService.getUserPurchasedFeatures(userId);

        return ResponseEntity.ok(ApiResponse.<List<UserPurchasedFeatureResponse>>builder()
                .message("User purchased features retrieved successfully")
                .data(response)
                .build());
    }
}
