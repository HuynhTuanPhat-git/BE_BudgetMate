package com.exe201.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_purchased_features",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "purchasable_feature_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPurchasedFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchasable_feature_id", nullable = false)
    PurchasableFeature purchasableFeature;

    @Column(name = "remaining_usage", nullable = false)
    Integer remainingUsage;

    @Column(name = "last_used_at")
    LocalDateTime lastUsedAt;

    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;
}
