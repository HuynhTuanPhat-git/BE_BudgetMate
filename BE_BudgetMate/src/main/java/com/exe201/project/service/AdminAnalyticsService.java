package com.exe201.project.service;

import com.exe201.project.dto.response.admin.DailyTransactionResponse;
import com.exe201.project.dto.response.admin.RevenueAnalyticsResponse;
import com.exe201.project.enums.RevenueAnalyticsPeriod;

import java.time.LocalDate;
import java.util.List;

public interface AdminAnalyticsService {
    
    /**
     * Get revenue analytics for specified period
     * @param period WEEKLY, MONTHLY, DAILY
     * @param startDate Start date for analysis
     * @param endDate End date for analysis (optional, defaults to today)
     * @return Revenue analytics response
     */
    RevenueAnalyticsResponse getRevenueAnalytics(RevenueAnalyticsPeriod period, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get membership revenue breakdown
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return List of membership revenue responses
     */
    List<com.exe201.project.dto.response.admin.MembershipRevenueResponse> getMembershipRevenueBreakdown(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get daily transaction statistics
     * @param date Specific date for transaction analysis
     * @return Daily transaction response
     */
    DailyTransactionResponse getDailyTransactions(LocalDate date);
}
