package com.exe201.project.controller;

import com.exe201.project.dto.request.SubscriptionRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.SubscriptionResponse;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.SubscriptionService;
import com.exe201.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final UserService userService;
    
    @PostMapping("/subscribe/{membershipPlanId}")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Subscribe to a membership plan")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribeToMembership(
            @PathVariable Long membershipPlanId,
            @Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse subscription = subscriptionService.subscribeToMembership(membershipPlanId, request);
        return ResponseEntity.ok(
                ApiResponse.<SubscriptionResponse>builder()
                        .message("Successfully subscribed to membership plan.")
                        .data(subscription)
                        .build()
        );
    }
    
    @GetMapping("/current")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current active subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getCurrentActiveSubscription() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        SubscriptionResponse subscription = subscriptionService.getCurrentActiveSubscription(userId);
        return ResponseEntity.ok(
                ApiResponse.<SubscriptionResponse>builder()
                        .message("Current active subscription retrieved successfully.")
                        .data(subscription)
                        .build()
        );
    }
    
    @GetMapping("/history")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get subscription history")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getSubscriptionHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionHistory(userId);
        return ResponseEntity.ok(
                ApiResponse.<List<SubscriptionResponse>>builder()
                        .message("Subscription history retrieved successfully.")
                        .data(subscriptions)
                        .build()
        );
    }
    
//    @DeleteMapping("/cancel")
//    @SecurityRequirement(name = "bearerAuth")
//    @Operation(summary = "Cancel current active subscription")
//    public ResponseEntity<ApiResponse<Void>> cancelCurrentSubscription() {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Long userId = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found")).getId();
//        subscriptionService.cancelCurrentSubscription(userId);
//        return ResponseEntity.ok(
//                ApiResponse.<Void>builder()
//                        .message("Subscription cancelled successfully.")
//                        .build()
//        );
//    }
}
