package com.exe201.project.controller;

import com.exe201.project.dto.request.PaymentRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.PaymentResponse;
import com.exe201.project.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
//    @PostMapping("/create/{membershipPlanId}")
//    @SecurityRequirement(name = "bearerAuth")
//    @Operation(summary = "Create payment link for membership plan")
//    public ResponseEntity<ApiResponse<PaymentResponse>> createPaymentLink(
//            @PathVariable Long membershipPlanId,
//            @Valid @RequestBody PaymentRequest request) {
//        try {
//            PaymentResponse paymentResponse = paymentService.createPaymentLink(membershipPlanId, request);
//            return ResponseEntity.ok(
//                    ApiResponse.<PaymentResponse>builder()
//                            .message("Payment link created successfully")
//                            .data(paymentResponse)
//                            .build()
//            );
//        } catch (Exception e) {
//            log.error("Error creating payment link: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(ApiResponse.<PaymentResponse>builder()
//                            .success(false)
//                            .message("Failed to create payment link: " + e.getMessage())
//                            .build());
//        }
//    }
    
    @GetMapping("/status/{orderCode}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get payment status")
    public ResponseEntity<ApiResponse<String>> getPaymentStatus(@PathVariable String orderCode) {
        try {
            String status = paymentService.getPaymentStatus(orderCode);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Payment status retrieved successfully")
                            .data(status)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Failed to get payment status: " + e.getMessage())
                            .build());
        }
    }
    
    @DeleteMapping("/cancel/{orderCode}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel payment")
    public ResponseEntity<ApiResponse<String>> cancelPayment(
            @PathVariable String orderCode,
            @RequestParam(required = false, defaultValue = "User cancelled") String reason) {
        try {
            paymentService.cancelPayment(orderCode, reason);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Payment cancelled successfully")
                            .data("Payment with order code " + orderCode + " has been cancelled")
                            .build()
            );
        } catch (Exception e) {
            log.error("Error cancelling payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Failed to cancel payment: " + e.getMessage())
                            .build());
        }
    }
    
//    @PostMapping("/webhook")
//    @Operation(summary = "Handle PayOS webhook")
//    public ResponseEntity<Map<String, Object>> handleWebhook(HttpServletRequest request) {
//        try {
//            String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
//            String signature = request.getHeader("X-PayOS-Signature");
//
//            log.info("Received webhook with signature: {}", signature);
//            log.debug("Webhook body: {}", body);
//
//            paymentService.handlePaymentWebhook(body, signature);
//
//            return ResponseEntity.ok(Map.of(
//                    "error", 0,
//                    "message", "Webhook processed successfully"
//            ));
//        } catch (Exception e) {
//            log.error("Error handling webhook: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of(
//                            "error", 1,
//                            "message", "Failed to process webhook: " + e.getMessage()
//                    ));
//        }
//    }
    
    @PostMapping("/confirm")
    @Operation(summary = "Confirm payment status (for testing)")
    public ResponseEntity<ApiResponse<String>> confirmPayment(
            @RequestParam String orderCode,
            @RequestParam String status) {
        try {
            paymentService.confirmPayment(orderCode, status);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Payment confirmation processed successfully")
                            .data("Payment status updated for order: " + orderCode)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error confirming payment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Failed to confirm payment: " + e.getMessage())
                            .build());
        }
    }
}
