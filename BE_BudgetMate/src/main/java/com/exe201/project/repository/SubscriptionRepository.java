package com.exe201.project.repository;

import com.exe201.project.entity.Subscription;
import com.exe201.project.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    List<Subscription> findByUserId(Long userId);
    
    List<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
    
    Optional<Subscription> findByOrderCode(String orderCode);
    
    @Query("SELECT s " +
            "FROM Subscription s " +
            "WHERE s.user.id = :userId " +
            "AND s.status = 'ACTIVE' " +
            "AND s.startDate <= :currentDate " +
            "AND s.endDate >= :currentDate")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT s FROM Subscription s WHERE s.membershipPlan.id = :membershipPlanId")
    List<Subscription> findByMembershipPlanId(@Param("membershipPlanId") Long membershipPlanId);
    
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :currentDate AND s.status = 'ACTIVE'")
    List<Subscription> findExpiredActiveSubscriptions(@Param("currentDate") LocalDate currentDate);
    
    // Admin Analytics Queries
    @Query("SELECT SUM(s.membershipPlan.price) FROM Subscription s WHERE s.paymentStatus = 'COMPLETED' AND s.startDate BETWEEN :startDate AND :endDate")
    Double getTotalRevenueBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.paymentStatus = 'COMPLETED' AND s.startDate BETWEEN :startDate AND :endDate")
    Long getTransactionCountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(DISTINCT s.user.id) FROM Subscription s WHERE s.paymentStatus = 'COMPLETED' AND s.startDate BETWEEN :startDate AND :endDate")
    Long getUniqueUserCountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT s FROM Subscription s WHERE s.paymentStatus = 'COMPLETED' AND s.startDate BETWEEN :startDate AND :endDate ORDER BY s.startDate")
    List<Subscription> findCompletedSubscriptionsBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT s.membershipPlan.id, s.membershipPlan.name, SUM(s.membershipPlan.price), COUNT(s) FROM Subscription s WHERE s.paymentStatus = 'COMPLETED' AND s.startDate BETWEEN :startDate AND :endDate GROUP BY s.membershipPlan.id, s.membershipPlan.name")
    List<Object[]> getMembershipRevenueBreakdown(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
