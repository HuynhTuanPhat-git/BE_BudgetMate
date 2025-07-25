package com.exe201.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "membership_features")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_plan_id", nullable = false)
    MembershipPlan membershipPlan;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    Feature feature;
    
    @Column(name = "limit_value")
    Integer limitValue; // null means unlimited, 0 means not allowed, >0 means specific limit
    
    @Column(name = "is_enabled")
    Boolean isEnabled = true;
    
    String description; // Additional description for this feature in this membership
}
