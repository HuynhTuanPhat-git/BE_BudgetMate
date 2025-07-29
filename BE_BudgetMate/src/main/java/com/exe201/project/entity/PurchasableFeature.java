package com.exe201.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "purchasable_features")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchasableFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    Feature feature;

    @Column(name = "credit_price", nullable = false)
    Integer creditPrice;

    @Column(name = "usage_limit")
    Integer usageLimit;

    @Column(name = "is_active")
    Boolean isActive = true;

    @Column(name = "description")
    String description;

    @Column(name = "target_membership_plans")
    String targetMembershipPlans;
}
