package com.exe201.project.repository;

import com.exe201.project.entity.User;
import com.exe201.project.entity.Wallets;
import com.exe201.project.enums.WalletStatus;
import com.exe201.project.enums.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallets, Long> {

    List<Wallets> findAllByUser_IdAndIsHiddenFalse(Long userId);

    
    List<Wallets> findByUserIdAndType(Long userId, WalletType type);
    
    @Query("SELECT w FROM Wallets w WHERE w.user.id = :userId AND w.name LIKE %:name%")
    List<Wallets> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("name") String name);
    
    @Query("SELECT SUM(w.balance) FROM Wallets w WHERE w.user.id = :userId")
    Double getTotalBalanceByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(w.balance) FROM Wallets w WHERE w.user.id = :userId AND w.type = :type")
    Double getTotalBalanceByUserIdAndType(@Param("userId") Long userId, @Param("type") WalletType type);
    
    Optional<Wallets> findByIdAndUserId(Long id, Long userId);
    
    boolean existsByUserIdAndName(Long userId, String name);

    List<Wallets> findAllByUserId(Long id);
    Optional<Wallets> findByUserAndType(User user, WalletType type);
    
    // Find wallets by user, type and status
    List<Wallets> findByUserAndTypeAndStatus(User user, WalletType type, WalletStatus status);
    
    // Find expired SAVINGS wallets that are still ACTIVE
    @Query("SELECT w FROM Wallets w WHERE w.type = 'SAVINGS' AND w.status = 'ACTIVE' AND w.deadline <= :currentDate")
    List<Wallets> findExpiredSavingsWallets(@Param("currentDate") LocalDate currentDate);
}
