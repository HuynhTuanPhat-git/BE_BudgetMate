package com.exe201.project.repository;

import com.exe201.project.entity.Transaction;
import com.exe201.project.entity.Wallets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByWalletIdAndIsDeletedFalseOrderByTransactionTimeDesc(Long walletId);
    
    List<Transaction> findByWalletIdAndIsDeletedFalse(Long walletId);
    
    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId AND t.transactionTime BETWEEN :startDate AND :endDate AND t.isDeleted = false")
    List<Transaction> findByWalletIdAndDateRange(@Param("walletId") Long walletId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.wallet.id = :walletId AND t.amount > 0 AND t.isDeleted = false")
    Double getTotalIncomeByWalletId(@Param("walletId") Long walletId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.wallet.id = :walletId AND t.amount < 0 AND t.isDeleted = false")
    Double getTotalExpenseByWalletId(@Param("walletId") Long walletId);

    List<Transaction> findByWalletAndTransactionTimeBetweenAndIsDeletedFalse(
            Wallets wallet, LocalDateTime startTime, LocalDateTime endTime);
            
    // Method to find deleted transactions for audit trail
    List<Transaction> findByWalletIdAndIsDeletedTrueOrderByUpdatedAtDesc(Long walletId);
}