package com.exe201.project.repository;

import com.exe201.project.entity.CreditTransaction;
import com.exe201.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {

    Page<CreditTransaction> findByUserOrderByTransactionTimeDesc(User user, Pageable pageable);
    List<CreditTransaction> findByUserAndTransactionTimeBetween(User user, LocalDateTime startTime, LocalDateTime endTime);
}
