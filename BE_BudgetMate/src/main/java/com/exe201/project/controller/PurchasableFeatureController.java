package com.exe201.project.controller;

import com.exe201.project.configuration.security.utils.AuthenticationUtil;
import com.exe201.project.dto.request.feature.CreatePurchasableFeatureRequest;
import com.exe201.project.dto.request.feature.UpdatePurchasableFeatureRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.PagedResponse;
import com.exe201.project.dto.response.purchasable_feature.PurchasableFeatureResponse;
import com.exe201.project.service.PurchasableFeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchasable-features")
@RequiredArgsConstructor
@Tag(name = "Purchasable Features", description = "APIs for managing purchasable features")
@SecurityRequirement(name = "bearerAuth")
public class PurchasableFeatureController {

    private final PurchasableFeatureService purchasableFeatureService;
    private final AuthenticationUtil authenticationUtil;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create purchasable feature",
            description = "Create a new feature that users can purchase with credits")
    public ResponseEntity<ApiResponse<PurchasableFeatureResponse>> createPurchasableFeature(
            @Valid @RequestBody CreatePurchasableFeatureRequest request) {
        PurchasableFeatureResponse response = purchasableFeatureService.createPurchasableFeature(request);
        return ResponseEntity.ok(ApiResponse.<PurchasableFeatureResponse>builder()
                .message("Purchasable feature created successfully")
                .data(response)
                .build());
    }

    @GetMapping
    @Operation(summary = "Get purchasable features",
            description = "Get paginated list of purchasable features")
    public ResponseEntity<ApiResponse<PagedResponse<PurchasableFeatureResponse>>> getPurchasableFeatures(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<PurchasableFeatureResponse> response = purchasableFeatureService.getPurchasableFeatures(pageable);
        return ResponseEntity.ok(ApiResponse.<PagedResponse<PurchasableFeatureResponse>>builder()
                .message("Purchasable features retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchasable feature by ID",
            description = "Get a specific purchasable feature by ID")
    public ResponseEntity<ApiResponse<PurchasableFeatureResponse>> getPurchasableFeature(@PathVariable UUID id) {
        PurchasableFeatureResponse response = purchasableFeatureService.getPurchasableFeature(id);
        return ResponseEntity.ok(ApiResponse.<PurchasableFeatureResponse>builder()
                .message("Purchasable feature retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/available-for-user")
    @Operation(summary = "Get purchasable features for the current user",
            description = "Get a list of purchasable features available for the current user")
    public ResponseEntity<ApiResponse<List<PurchasableFeatureResponse>>> getAvailableFeaturesForUser() {
        Long userId = authenticationUtil.getCurrentUserId();
        List<PurchasableFeatureResponse> response = purchasableFeatureService.getAvailableFeaturesForUser(userId);
        return ResponseEntity.ok(ApiResponse.<List<PurchasableFeatureResponse>>builder()
                .message("Available purchasable features retrieved successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update purchasable feature",
            description = "Update an existing purchasable feature - supports partial updates")
    public ResponseEntity<ApiResponse<PurchasableFeatureResponse>> updatePurchasableFeature(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePurchasableFeatureRequest request) {
        PurchasableFeatureResponse response = purchasableFeatureService.updatePurchasableFeature(id, request);
        return ResponseEntity.ok(ApiResponse.<PurchasableFeatureResponse>builder()
                .message("Purchasable feature updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete purchasable feature",
            description = "Delete a purchasable feature")
    public ResponseEntity<ApiResponse<Void>> deletePurchasableFeature(@PathVariable UUID id) {
        purchasableFeatureService.deletePurchasableFeature(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Purchasable feature deleted successfully")
                .build());
    }
}