package com.exe201.project.repository;

import com.exe201.project.entity.CreditTransaction;
import com.exe201.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {

    Page<CreditTransaction> findByUserOrderByTransactionTimeDesc(User user, Pageable pageable);

    @Query("SELECT COALESCE(SUM(ct.creditSpent), 0) " +
            "FROM CreditTransaction ct " +
            "WHERE ct.user = :user")
    Long getTotalCreditSpentByUser(@Param("user") User user);

    @Query("SELECT COUNT(ct) " +
            "FROM CreditTransaction ct " +
            "WHERE ct.user = :user AND ct.purchasableFeature.feature.featureKey = :featureKey")
    Long countFeaturePurchasesByUser(@Param("user") User user, @Param("featureKey") String featureKey);

    List<CreditTransaction> findByUserAndTransactionTimeBetween(User user, LocalDateTime startTime, LocalDateTime endTime);
}
