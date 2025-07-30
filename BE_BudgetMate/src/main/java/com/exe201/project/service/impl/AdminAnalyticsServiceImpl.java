package com.exe201.project.service.impl;

import com.exe201.project.dto.response.admin.DailyRevenueResponse;
import com.exe201.project.dto.response.admin.DailyTransactionResponse;
import com.exe201.project.dto.response.admin.MembershipRevenueResponse;
import com.exe201.project.dto.response.admin.RevenueAnalyticsResponse;
import com.exe201.project.entity.Subscription;
import com.exe201.project.enums.RevenueAnalyticsPeriod;
import com.exe201.project.repository.SubscriptionRepository;
import com.exe201.project.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public RevenueAnalyticsResponse getRevenueAnalytics(RevenueAnalyticsPeriod period, LocalDate startDate, LocalDate endDate) {
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Calculate period-specific dates
        LocalDate calculatedStartDate = calculateStartDate(period, endDate);
        if (startDate != null) {
            calculatedStartDate = startDate;
        }

        log.info("Getting revenue analytics for period {} from {} to {}", period, calculatedStartDate, endDate);

        // Get total revenue
        Double totalRevenue = subscriptionRepository.getTotalRevenueBetweenDates(calculatedStartDate, endDate);
        if (totalRevenue == null) {
            totalRevenue = 0.0;
        }

        // Get daily breakdown
        List<DailyRevenueResponse> dailyBreakdown = getDailyRevenueBreakdown(calculatedStartDate, endDate);

        // Get membership breakdown
        List<MembershipRevenueResponse> membershipBreakdown = getMembershipRevenueBreakdown(calculatedStartDate, endDate);

        return RevenueAnalyticsResponse.builder()
                .totalRevenue(totalRevenue)
                .startDate(calculatedStartDate)
                .endDate(endDate)
                .period(period.name())
                .dailyBreakdown(dailyBreakdown)
                .membershipBreakdown(membershipBreakdown)
                .build();
    }

    @Override
    public List<MembershipRevenueResponse> getMembershipRevenueBreakdown(LocalDate startDate, LocalDate endDate) {
        log.info("Getting membership revenue breakdown from {} to {}", startDate, endDate);
        
        // Debug: Get all subscriptions in date range first
        List<Subscription> allSubscriptions = subscriptionRepository.findCompletedSubscriptionsBetweenDates(startDate, endDate);
        log.info("Found {} paid subscriptions in date range", allSubscriptions.size());
        
        // Debug: Check payment statuses
        if (allSubscriptions.isEmpty()) {
            log.warn("No paid subscriptions found. Let's check all subscriptions in this period.");
            // Get all subscriptions regardless of payment status for debugging
            List<Subscription> allSubsRegardlessOfStatus = subscriptionRepository.findAll()
                    .stream()
                    .filter(s -> !s.getStartDate().isBefore(startDate) && !s.getStartDate().isAfter(endDate))
                    .toList();
            log.info("Total subscriptions in date range (any status): {}", allSubsRegardlessOfStatus.size());
            
            allSubsRegardlessOfStatus.forEach(s -> 
                log.info("Subscription: ID={}, StartDate={}, PaymentStatus={}, MembershipPlan={}", 
                    s.getId(), s.getStartDate(), s.getPaymentStatus(), 
                    s.getMembershipPlan() != null ? s.getMembershipPlan().getName() : "null"));
        }
        
        List<Object[]> results = subscriptionRepository.getMembershipRevenueBreakdown(startDate, endDate);
        log.info("Query returned {} membership revenue results", results.size());
        
        return results.stream()
                .map(row -> {
                    log.info("Processing row: MembershipId={}, Name={}, Revenue={}, Count={}", 
                        row[0], row[1], row[2], row[3]);
                    return MembershipRevenueResponse.builder()
                            .membershipPlanId((Long) row[0])
                            .membershipName((String) row[1])
                            .revenue((Double) row[2])
                            .subscriptionCount(((Long) row[3]).intValue())
                            .averageOrderValue((Double) row[2] / ((Long) row[3]).intValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public DailyTransactionResponse getDailyTransactions(LocalDate date) {
        LocalDate startOfDay = date;
        LocalDate endOfDay = date;

        Long totalTransactions = subscriptionRepository.getTransactionCountBetweenDates(startOfDay, endOfDay);
        Double totalAmount = subscriptionRepository.getTotalRevenueBetweenDates(startOfDay, endOfDay);
        Long uniqueUsers = subscriptionRepository.getUniqueUserCountBetweenDates(startOfDay, endOfDay);

        if (totalTransactions == null) totalTransactions = 0L;
        if (totalAmount == null) totalAmount = 0.0;
        if (uniqueUsers == null) uniqueUsers = 0L;

        return DailyTransactionResponse.builder()
                .date(date)
                .totalTransactions(totalTransactions.intValue())
                .successfulTransactions(totalTransactions.intValue()) // All paid subscriptions are successful
                .failedTransactions(0) // We don't track failed transactions in current model
                .totalAmount(totalAmount)
                .uniqueUsers(uniqueUsers.intValue())
                .build();
    }

    private LocalDate calculateStartDate(RevenueAnalyticsPeriod period, LocalDate endDate) {
        return switch (period) {
            case WEEKLY -> endDate.minusWeeks(1);
            case MONTHLY -> endDate.minusMonths(1);
            case DAILY -> endDate;
        };
    }

    private List<DailyRevenueResponse> getDailyRevenueBreakdown(LocalDate startDate, LocalDate endDate) {
        List<Subscription> subscriptions = subscriptionRepository.findCompletedSubscriptionsBetweenDates(startDate, endDate);
        
        Map<LocalDate, List<Subscription>> subscriptionsByDate = subscriptions.stream()
                .collect(Collectors.groupingBy(Subscription::getStartDate));

        List<DailyRevenueResponse> dailyBreakdown = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            List<Subscription> daySubscriptions = subscriptionsByDate.getOrDefault(currentDate, new ArrayList<>());
            
            Double dayRevenue = daySubscriptions.stream()
                    .mapToDouble(s -> s.getMembershipPlan().getPrice())
                    .sum();
            
            Integer dayTransactionCount = daySubscriptions.size();
            Integer dayUserCount = (int) daySubscriptions.stream()
                    .map(s -> s.getUser().getId())
                    .distinct()
                    .count();

            dailyBreakdown.add(DailyRevenueResponse.builder()
                    .date(currentDate)
                    .revenue(dayRevenue)
                    .transactionCount(dayTransactionCount)
                    .userCount(dayUserCount)
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return dailyBreakdown;
    }
}
