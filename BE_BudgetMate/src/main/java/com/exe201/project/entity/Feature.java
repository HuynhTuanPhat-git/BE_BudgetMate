package com.exe201.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Feature {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    @Column(unique = true, nullable = false)
    String name;
    
    @Column(nullable = false)
    String description;
    
    @Column(name = "feature_key", unique = true, nullable = false)
    String featureKey; // Unique identifier for feature (e.g., "CREATE_MULTIPLE_WALLETS")
    
    @Column(name = "is_active")
    Boolean isActive = true;
    
    @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, orphanRemoval = true)
    List<MembershipFeature> membershipFeatures;
}
