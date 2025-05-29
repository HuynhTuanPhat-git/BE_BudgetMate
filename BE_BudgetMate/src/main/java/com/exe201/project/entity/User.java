package com.exe201.project.entity;

import com.exe201.project.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "full_name")
    String fullName;

    @Email
    String email;

    @Column(unique = true, nullable = false)
    String password;

    @Column(name = "streak_days")
    Integer streakDays;

    @Column(name = "last_login_date")
    LocalDateTime lastLoginDate;

    Integer credits;

    @Enumerated(EnumType.STRING)
    UserStatus status;

    @ManyToOne
    @JoinColumn(name = "role_id")
    Role role;
    
    @ManyToOne
    @JoinColumn(name = "pet_id")
    Pet pet;
    
    @OneToMany(mappedBy = "user")
    List<Wallets> wallets;
    
    @OneToMany(mappedBy = "user")
    List<Subscription> subscriptions;
    
    @OneToMany(mappedBy = "user")
    List<QuizLog> quizLogs;
}
