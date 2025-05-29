package com.exe201.project.entity;

import com.exe201.project.enums.PaymentMethod;
import com.exe201.project.enums.PaymentStatus;
import com.exe201.project.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    @Column(name = "start_date")
    LocalDate startDate;
    
    @Column(name = "end_date")
    LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    SubscriptionStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    PaymentStatus paymentStatus;
    
    @ManyToOne
    @JoinColumn(name = "membership_plan_id")
    MembershipPlan membershipPlan;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}
