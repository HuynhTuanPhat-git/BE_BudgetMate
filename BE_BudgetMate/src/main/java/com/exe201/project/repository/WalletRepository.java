package com.exe201.project.repository;

import com.exe201.project.entity.Wallets;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallets, Long> {
}
