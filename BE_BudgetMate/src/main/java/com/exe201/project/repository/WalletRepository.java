package com.exe201.project.repository;

import com.exe201.project.entity.Wallets;
import com.exe201.project.enums.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallets, Long> {
    
    List<Wallets> findByUserId(Long userId);
    
    List<Wallets> findByUserIdAndType(Long userId, WalletType type);
    
    @Query("SELECT w FROM Wallets w WHERE w.user.id = :userId AND w.name LIKE %:name%")
    List<Wallets> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("name") String name);
    
    @Query("SELECT SUM(w.balance) FROM Wallets w WHERE w.user.id = :userId")
    Double getTotalBalanceByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(w.balance) FROM Wallets w WHERE w.user.id = :userId AND w.type = :type")
    Double getTotalBalanceByUserIdAndType(@Param("userId") Long userId, @Param("type") WalletType type);
    
    Optional<Wallets> findByIdAndUserId(Long id, Long userId);
    
    boolean existsByUserIdAndName(Long userId, String name);
}
