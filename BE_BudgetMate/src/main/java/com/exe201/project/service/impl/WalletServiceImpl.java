package com.exe201.project.service.impl;

import com.exe201.project.dto.request.WalletRequest;
import com.exe201.project.dto.response.WalletResponse;
import com.exe201.project.entity.Transaction;
import com.exe201.project.entity.User;
import com.exe201.project.entity.Wallets;
import com.exe201.project.enums.WalletType;
import com.exe201.project.exception.InsufficientBalanceException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.mapper.WalletMapper;
import com.exe201.project.repository.TransactionRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.repository.WalletRepository;
import com.exe201.project.service.UserService;
import com.exe201.project.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {
    
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletMapper walletMapper;
    private final UserService userService;

    @Override
    public WalletResponse createWallet(WalletRequest request) {
        // Get current user from security context
        User user = userService.getAuthenticatedUser();

        Wallets wallet = new Wallets();
        wallet.setType(request.type());
        wallet.setName(request.name());
        wallet.setBalance(0.0); // Initial balance is 0
        wallet.setTargetAmount(request.targetAmount());
        wallet.setInterestRate(request.interestRate());
        wallet.setDeadline(request.deadline());
        wallet.setUser(user);

        Wallets savedWallet = walletRepository.save(wallet);
        return walletMapper.toWalletResponse(savedWallet);
    }

    @Override
    public List<WalletResponse> getAllWallet() {
        User user = userService.getAuthenticatedUser();
        
        List<Wallets> wallets = walletRepository.findByUserId(user.getId());
        return wallets.stream()
                .map(walletMapper::toWalletResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WalletResponse getDetailWallet(String walletId) {
        Long id = Long.parseLong(walletId);
        Wallets wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
        
        // Check if wallet belongs to current user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!wallet.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Wallet not found");
        }
        
        return walletMapper.toWalletResponse(wallet);
    }

    @Override
    public WalletResponse updateWallet(String walletId, WalletRequest request) {
        Long id = Long.parseLong(walletId);
        Wallets wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
        
        // Check if wallet belongs to current user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!wallet.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Wallet not found");
        }

        wallet.setType(request.type());
        wallet.setName(request.name());
        wallet.setTargetAmount(request.targetAmount());
        wallet.setInterestRate(request.interestRate());
        wallet.setDeadline(request.deadline());

        Wallets updatedWallet = walletRepository.save(wallet);
        return walletMapper.toWalletResponse(updatedWallet);
    }

    @Override
    public void deleteWallet(String walletId) {
        Long id = Long.parseLong(walletId);
        Wallets wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
        
        // Check if wallet belongs to current user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!wallet.getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Wallet not found");
        }

        walletRepository.delete(wallet);
    }

    @Override
    public List<WalletResponse> getWalletsByUserId(Long userId) {
        List<Wallets> wallets = walletRepository.findByUserId(userId);
        return wallets.stream()
                .map(walletMapper::toWalletResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Double getTotalBalance() {
        User user = userService.getAuthenticatedUser();

        Double totalBalance = walletRepository.getTotalBalanceByUserId(user.getId());
        totalBalance = totalBalance != null ? totalBalance : 0.0;
        return totalBalance;
    }

    public Double getTotalBalanceByType(String type) {
        User user = userService.getAuthenticatedUser();

        WalletType walletType = WalletType.valueOf(type.toUpperCase());
        Double totalBalance = walletRepository.getTotalBalanceByUserIdAndType(user.getId(), walletType);
        totalBalance = totalBalance != null ? totalBalance : 0.0;
        return totalBalance;
    }
}
