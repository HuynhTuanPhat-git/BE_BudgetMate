package com.exe201.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_transactions")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchasable_feature_id")
    PurchasableFeature purchasableFeature;

    @Column(name = "credit_spent", nullable = false)
    Integer creditSpent;

    @Column(name = "usage_granted")
    Integer usageGranted;

    @Column(name = "transaction_time", nullable = false)
    LocalDateTime transactionTime;

    @Column(name = "description")
    String description;
}