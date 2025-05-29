package com.exe201.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
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
}
