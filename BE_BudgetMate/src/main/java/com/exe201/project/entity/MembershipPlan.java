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
public class MembershipPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    String name;
    
    String description;
    
    Double price;
    
    Double duration; // in months
    
    @OneToMany(mappedBy = "membershipPlan")
    List<Subscription> subscriptions;
}
