package com.exe201.project.controller;

import com.exe201.project.configuration.security.utils.AuthenticationUtil;
import com.exe201.project.dto.request.feature.PurchaseFeatureRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.PagedResponse;
import com.exe201.project.dto.response.credit_transaction.CreditTransactionResponse;
import com.exe201.project.dto.response.feature.FeaturePurchaseResponse;
import com.exe201.project.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/credits")
@RequiredArgsConstructor
@Tag(name = "Credit Management", description = "APIs for managing user credits and purchasing features")
@SecurityRequirement(name = "bearerAuth")
public class CreditController {

    private final CreditService creditService;
    private final AuthenticationUtil authenticationUtil;

    @PostMapping("/purchase-feature")
    @Operation(
            summary = "Purchase feature with credits",
            description = "Allow Basic and Plus users to purchase additional features using credits. Premium users have access to all features."
    )
    public ResponseEntity<ApiResponse<FeaturePurchaseResponse>> purchaseFeature(
            @Valid @RequestBody PurchaseFeatureRequest request) {
        Long userId = authenticationUtil.getCurrentUserId();
        FeaturePurchaseResponse response = creditService.purchaseFeature(userId, request);

        return ResponseEntity.ok(ApiResponse.<FeaturePurchaseResponse>builder()
                .message("Feature purchased successfully")
                .data(response)
                .build());
    }

    @GetMapping("/transactions")
    @Operation(
            summary = "Get credit transaction history",
            description = "Retrieve paginated credit transaction history for the current user"
    )
    public ResponseEntity<ApiResponse<PagedResponse<CreditTransactionResponse>>> getCreditTransactionHistory(
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {

        Long userId = authenticationUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionTime").descending());
        PagedResponse<CreditTransactionResponse> response = creditService.getCreditTransactionHistory(userId, pageable);

        return ResponseEntity.ok(ApiResponse.<PagedResponse<CreditTransactionResponse>>builder()
                .message("Credit transaction history retrieved successfully")
                .data(response)
                .build());
    }
}