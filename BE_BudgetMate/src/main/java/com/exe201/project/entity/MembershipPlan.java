package com.exe201.project.entity;

import com.exe201.project.enums.DurationType;
import com.exe201.project.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    String name;
    
    String description;
    
    Double price;
    
    Double duration; // in months

    DurationType type;

    @Enumerated(EnumType.STRING)
    Status status;
    
    @OneToMany(mappedBy = "membershipPlan")
    List<Subscription> subscriptions = new ArrayList<>();
    
    @OneToMany(mappedBy = "membershipPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<MembershipFeature> membershipFeatures = new ArrayList<>();
    
    public MembershipPlan(String name, String description, Double price, Double duration, DurationType type, Status status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.duration = duration;
        this.type = type;
        this.status = status;
        this.subscriptions = new ArrayList<>();
        this.membershipFeatures = new ArrayList<>();
    }
}
