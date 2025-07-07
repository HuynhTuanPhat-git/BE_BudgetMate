package com.exe201.project.controller;

import com.exe201.project.dto.request.MembershipRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.MembershipResponse;
import com.exe201.project.service.MembershipPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/memberships")
@RequiredArgsConstructor
public class MembershipController {
    
    private final MembershipPlanService membershipPlanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<MembershipResponse>> createMembershipPlan(@Valid @RequestBody MembershipRequest request) {
        MembershipResponse membership = membershipPlanService.createMembershipPlan(request);
        return ResponseEntity.ok(
                ApiResponse.<MembershipResponse>builder()
                        .message("Membership plan created successfully.")
                        .data(membership)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MembershipResponse>>> getAllMembershipPlans() {
        List<MembershipResponse> memberships = membershipPlanService.getAllMembershipPlan();
        return ResponseEntity.ok(
                ApiResponse.<List<MembershipResponse>>builder()
                        .message("Membership plans retrieved successfully.")
                        .data(memberships)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MembershipResponse>> getMembershipPlan(@PathVariable Long id) {
        MembershipResponse membership = membershipPlanService.getMembershipPlan(id);
        return ResponseEntity.ok(
                ApiResponse.<MembershipResponse>builder()
                        .message("Membership plan retrieved successfully.")
                        .data(membership)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MembershipResponse>> updateMembershipPlan(
            @PathVariable Long id, 
            @Valid @RequestBody MembershipRequest request) {
        MembershipResponse membership = membershipPlanService.updateMembershipPlan(id, request);
        return ResponseEntity.ok(
                ApiResponse.<MembershipResponse>builder()
                        .message("Membership plan updated successfully.")
                        .data(membership)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMembershipPlan(@PathVariable Long id) {
        membershipPlanService.deleteMembershipPlan(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Membership plan deleted successfully.")
                        .build()
        );
    }

    @GetMapping("/{id}/features/{featureKey}/access")
    public ResponseEntity<ApiResponse<Boolean>> checkFeatureAccess(
            @PathVariable Long id, 
            @PathVariable String featureKey) {
        boolean hasAccess = membershipPlanService.hasFeatureAccess(id, featureKey);
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .message("Feature access check completed.")
                        .data(hasAccess)
                        .build()
        );
    }

    @GetMapping("/{id}/features/{featureKey}/limit")
    public ResponseEntity<ApiResponse<Integer>> getFeatureLimit(
            @PathVariable Long id, 
            @PathVariable String featureKey) {
        Integer limit = membershipPlanService.getFeatureLimit(id, featureKey);
        return ResponseEntity.ok(
                ApiResponse.<Integer>builder()
                        .message("Feature limit retrieved successfully.")
                        .data(limit)
                        .build()
        );
    }
}
