package com.exe201.project.entity;

import com.exe201.project.enums.WalletStatus;
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

    @Enumerated(EnumType.STRING)
    WalletStatus status = WalletStatus.ACTIVE;

    String name;

    double balance;

    @Column(name = "target_amount")
    double targetAmount;

    @Column(name = "interest_rate")
    double interestRate;

    // Ngày bắt đầu gửi tiết kiệm (cho SAVINGS wallet)
    @Column(name = "start_date")
    LocalDate startDate;

    // Kì hạn tính bằng tháng (cho SAVINGS wallet)
    @Column(name = "term_months")
    Integer termMonths;

    LocalDate deadline;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
    
    @OneToMany(mappedBy = "wallet")
    List<Transaction> transactions;

    boolean isHidden;
}
