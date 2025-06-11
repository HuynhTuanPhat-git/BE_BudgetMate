package com.exe201.project.repository;

import com.exe201.project.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByWalletIdOrderByTransactionTimeDesc(Long walletId);
    
    List<Transaction> findByWalletId(Long walletId);
    
    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId AND t.transactionTime BETWEEN :startDate AND :endDate")
    List<Transaction> findByWalletIdAndDateRange(@Param("walletId") Long walletId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.wallet.id = :walletId AND t.amount > 0")
    Double getTotalIncomeByWalletId(@Param("walletId") Long walletId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.wallet.id = :walletId AND t.amount < 0")
    Double getTotalExpenseByWalletId(@Param("walletId") Long walletId);
}
