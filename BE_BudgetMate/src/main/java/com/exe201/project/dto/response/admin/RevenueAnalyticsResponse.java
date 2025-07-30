package com.exe201.project.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalyticsResponse {
    private Double totalRevenue;
    private LocalDate startDate;
    private LocalDate endDate;
    private String period; // WEEKLY, MONTHLY
    private List<DailyRevenueResponse> dailyBreakdown;
    private List<MembershipRevenueResponse> membershipBreakdown;
}
