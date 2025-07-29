package com.exe201.project.controller;

import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.admin.DailyTransactionResponse;
import com.exe201.project.dto.response.admin.MembershipRevenueResponse;
import com.exe201.project.dto.response.admin.RevenueAnalyticsResponse;
import com.exe201.project.enums.RevenueAnalyticsPeriod;
import com.exe201.project.service.AdminAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
@Tag(name = "Admin Analytics", description = "Admin analytics and revenue tracking endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/revenue")
    @Operation(summary = "Get revenue analytics", description = "Get revenue analytics for specified period (weekly, monthly)")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getRevenueAnalytics(
            @Parameter(description = "Analysis period") 
            @RequestParam RevenueAnalyticsPeriod period,
            
            @Parameter(description = "Start date (optional, defaults based on period)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date (optional, defaults to today)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        RevenueAnalyticsResponse analytics = adminAnalyticsService.getRevenueAnalytics(period, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.<RevenueAnalyticsResponse>builder()
                .success(true)
                .message("Revenue analytics retrieved successfully")
                .data(analytics)
                .build());
    }

    @GetMapping("/revenue/membership")
    @Operation(summary = "Get membership revenue breakdown", description = "Get revenue breakdown by membership plans")
    public ResponseEntity<ApiResponse<List<MembershipRevenueResponse>>> getMembershipRevenueBreakdown(
            @Parameter(description = "Start date")
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date")
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<MembershipRevenueResponse> breakdown = adminAnalyticsService.getMembershipRevenueBreakdown(startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.<List<MembershipRevenueResponse>>builder()
                .success(true)
                .message("Membership revenue breakdown retrieved successfully")
                .data(breakdown)
                .build());
    }

    @GetMapping("/transactions/daily")
    @Operation(summary = "Get daily transaction statistics", description = "Get transaction statistics for a specific day")
    public ResponseEntity<ApiResponse<DailyTransactionResponse>> getDailyTransactions(
            @Parameter(description = "Date for analysis (defaults to today)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        DailyTransactionResponse transactions = adminAnalyticsService.getDailyTransactions(date);
        
        return ResponseEntity.ok(ApiResponse.<DailyTransactionResponse>builder()
                .success(true)
                .message("Daily transaction statistics retrieved successfully")
                .data(transactions)
                .build());
    }

    @GetMapping("/revenue/weekly")
    @Operation(summary = "Get weekly revenue", description = "Get revenue analytics for the current week")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getWeeklyRevenue() {
        RevenueAnalyticsResponse analytics = adminAnalyticsService.getRevenueAnalytics(
                RevenueAnalyticsPeriod.WEEKLY, null, null);
        
        return ResponseEntity.ok(ApiResponse.<RevenueAnalyticsResponse>builder()
                .success(true)
                .message("Weekly revenue analytics retrieved successfully")
                .data(analytics)
                .build());
    }

    @GetMapping("/revenue/monthly")
    @Operation(summary = "Get monthly revenue", description = "Get revenue analytics for the current month")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getMonthlyRevenue() {
        RevenueAnalyticsResponse analytics = adminAnalyticsService.getRevenueAnalytics(
                RevenueAnalyticsPeriod.MONTHLY, null, null);
        
        return ResponseEntity.ok(ApiResponse.<RevenueAnalyticsResponse>builder()
                .success(true)
                .message("Monthly revenue analytics retrieved successfully")
                .data(analytics)
                .build());
    }
}
