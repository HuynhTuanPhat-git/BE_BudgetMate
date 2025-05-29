package com.exe201.project.entity;

import com.exe201.project.enums.WalletType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wallets {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    @Enumerated(EnumType.STRING)
    WalletType type;

    String name;

    double balance;

    @Column(name = "target_amount")
    double targetAmount;

    @Column(name = "interest_rate")
    double interestRate;

    LocalDate deadline;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
    
    @OneToMany(mappedBy = "wallet")
    List<Transaction> transactions;
}
