package com.exe201.project.controller;

import com.exe201.project.dto.request.FeatureRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.FeatureResponse;
import com.exe201.project.service.FeatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/features")
@RequiredArgsConstructor
public class FeatureController {
    
    private final FeatureService featureService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<FeatureResponse>> createFeature(@Valid @RequestBody FeatureRequest request) {
        FeatureResponse feature = featureService.createFeature(request);
        return ResponseEntity.ok(
                ApiResponse.<FeatureResponse>builder()
                        .message("Feature created successfully.")
                        .data(feature)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeatureResponse>>> getAllFeatures() {
        List<FeatureResponse> features = featureService.getAllFeatures();
        return ResponseEntity.ok(
                ApiResponse.<List<FeatureResponse>>builder()
                        .message("Features retrieved successfully.")
                        .data(features)
                        .build()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<FeatureResponse>>> getActiveFeatures() {
        List<FeatureResponse> features = featureService.getActiveFeatures();
        return ResponseEntity.ok(
                ApiResponse.<List<FeatureResponse>>builder()
                        .message("Active features retrieved successfully.")
                        .data(features)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeatureResponse>> getFeatureById(@PathVariable Long id) {
        FeatureResponse feature = featureService.getFeatureById(id);
        return ResponseEntity.ok(
                ApiResponse.<FeatureResponse>builder()
                        .message("Feature retrieved successfully.")
                        .data(feature)
                        .build()
        );
    }

    @GetMapping("/key/{featureKey}")
    public ResponseEntity<ApiResponse<FeatureResponse>> getFeatureByKey(@PathVariable String featureKey) {
        FeatureResponse feature = featureService.getFeatureByKey(featureKey);
        return ResponseEntity.ok(
                ApiResponse.<FeatureResponse>builder()
                        .message("Feature retrieved successfully.")
                        .data(feature)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FeatureResponse>>> searchFeatures(@RequestParam String keyword) {
        List<FeatureResponse> features = featureService.searchFeatures(keyword);
        return ResponseEntity.ok(
                ApiResponse.<List<FeatureResponse>>builder()
                        .message("Feature search results retrieved successfully.")
                        .data(features)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeatureResponse>> updateFeature(
            @PathVariable Long id, 
            @Valid @RequestBody FeatureRequest request) {
        FeatureResponse feature = featureService.updateFeature(id, request);
        return ResponseEntity.ok(
                ApiResponse.<FeatureResponse>builder()
                        .message("Feature updated successfully.")
                        .data(feature)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeature(@PathVariable Long id) {
        featureService.deleteFeature(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Feature deleted successfully.")
                        .build()
        );
    }
}
