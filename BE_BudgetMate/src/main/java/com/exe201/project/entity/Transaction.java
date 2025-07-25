package com.exe201.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    Double amount;
    
    String description;
    
    @Column(name = "transaction_time")
    LocalDateTime transactionTime;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;
    
    @ManyToOne
    @JoinColumn(name = "wallet_id")
    Wallets wallet;

    @Column(name = "is_deleted")
    boolean isDeleted = false;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
    
    // Store original values before update for audit trail
    @Column(name = "original_amount")
    Double originalAmount;
    
    @Column(name = "original_description")
    String originalDescription;
    
    @Column(name = "original_transaction_time")
    LocalDateTime originalTransactionTime;
}
