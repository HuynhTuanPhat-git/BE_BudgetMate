package com.exe201.project.service.impl;

import com.exe201.project.dto.request.WalletRequest;
import com.exe201.project.dto.response.WalletResponse;
import com.exe201.project.entity.Transaction;
import com.exe201.project.entity.User;
import com.exe201.project.entity.Wallets;
import com.exe201.project.enums.WalletStatus;
import com.exe201.project.enums.WalletType;
import com.exe201.project.exception.InsufficientBalanceException;
import com.exe201.project.exception.OutOfPermissionException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.exception.WrongTypeException;
import com.exe201.project.mapper.WalletMapper;
import com.exe201.project.repository.TransactionRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.repository.WalletRepository;
import com.exe201.project.service.MembershipAccessService;
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
    private final MembershipAccessService membershipAccessService;

    @Override
    public WalletResponse createWallet(WalletRequest request) {
        // Get current user from security context
        User user = userService.getAuthenticatedUser();

        // Check if user has permission to create wallets
        if (!membershipAccessService.canCreateWallet(user.getId())) {
            throw new OutOfPermissionException("You don't have permission to create wallets");
        }

        // Check wallet limits based on membership
        List<Wallets> existingWallets = walletRepository.findAllByUserId(user.getId());
        Integer walletLimit = membershipAccessService.getFeatureLimit(user.getId(), "CREATE_WALLET");
        
        // For Basic plan (limit = 3), check if user can create wallet of this specific type
        if (walletLimit != null && walletLimit == 3) {
            // Basic plan: check if user already has a wallet of this type
            boolean hasWalletOfType = existingWallets.stream()
                    .anyMatch(wallet -> wallet.getType().equals(request.type()));
            
            if (hasWalletOfType) {
                throw new OutOfPermissionException("You already have a " + request.type() + " wallet. Basic plan allows only 1 wallet of each type (DEFAULT, DEBT, SAVINGS).");
            }
        } else if (walletLimit != null && existingWallets.size() >= walletLimit) {
            // For other plans, check total wallet limit
            throw new OutOfPermissionException("You have reached the maximum number of wallets allowed for your membership plan");
        }
        
        // Check if user can create multiple wallets (non-default wallets)
        if (!request.type().equals(WalletType.DEFAULT) && !membershipAccessService.canCreateMultipleWallets(user.getId())) {
            throw new OutOfPermissionException("Creating multiple wallets is only available for Plus and Premium members");
        }

        Wallets wallet = new Wallets();
        wallet.setType(request.type());
        wallet.setName(request.name());
        wallet.setBalance(0.0); // Initial balance is 0
        wallet.setTargetAmount(request.targetAmount());
        wallet.setInterestRate(request.interestRate() == 0.0 ? 0 : request.interestRate());
        wallet.setDeadline(request.deadline() == null ? null : request.deadline());
        
        // Handle SAVINGS wallet specific fields
        if (request.type().equals(WalletType.SAVINGS)) {
            // Validate required fields for SAVINGS wallet
            if (request.startDate() == null) {
                throw new IllegalArgumentException("Start date is required for SAVINGS wallet");
            }
            if (request.termMonths() == null || request.termMonths() < 1) {
                throw new IllegalArgumentException("Term months must be at least 1 for SAVINGS wallet");
            }
            if (request.interestRate() <= 0) {
                throw new IllegalArgumentException("Interest rate must be greater than 0 for SAVINGS wallet");
            }
            
            wallet.setStartDate(request.startDate());
            wallet.setTermMonths(request.termMonths());
            
            // Calculate deadline based on start date and term
            wallet.setDeadline(request.startDate().plusMonths(request.termMonths()));
        }
        
        if (request.type().equals(WalletType.DEFAULT) && 
                (request.interestRate() != 0 ||
                request.deadline() != null)) {
            throw new WrongTypeException("Default wallet must not have interest rate and deadline");
        }
        wallet.setUser(user);
        wallet.setTransactions(null);

        Wallets savedWallet = walletRepository.save(wallet);
        return walletMapper.toWalletResponse(savedWallet);
    }

    @Override
    public List<WalletResponse> getAllWallet() {
        User user = userService.getAuthenticatedUser();
        
        List<Wallets> wallets = walletRepository.findAllByUser_IdAndIsHiddenFalse(user.getId());
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

        if (!request.type().equals(wallet.getType())) {
            throw new WrongTypeException("Can not change wallet type.");
        }
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

        if (wallet.isHidden()) {
            throw new IllegalArgumentException("Wallet is already hidden");
        } else {
            wallet.setHidden(true);
        }
    }

    @Override
    public List<WalletResponse> getWalletsByUserId(Long userId) {
        List<Wallets> wallets = walletRepository.findAllByUser_IdAndIsHiddenFalse(userId);
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

    @Override
    public void processExpiredSavingsWallets() {
        // This method delegates to the scheduler for manual testing
        // In a real application, you might want to inject the scheduler or create a separate service
        // For now, we'll implement the logic directly here
        
        List<Wallets> expiredSavingsWallets = walletRepository.findExpiredSavingsWallets(java.time.LocalDate.now());
        
        for (Wallets savingsWallet : expiredSavingsWallets) {
            try {
                // Calculate maturity amount
                double maturityAmount = calculateMaturityAmount(
                    savingsWallet.getTargetAmount(),
                    savingsWallet.getInterestRate(),
                    savingsWallet.getTermMonths()
                );
                
                // Find user's DEFAULT wallet
                java.util.Optional<Wallets> defaultWalletOpt = walletRepository.findByUserAndType(
                    savingsWallet.getUser(), WalletType.DEFAULT);
                
                if (defaultWalletOpt.isPresent()) {
                    Wallets defaultWallet = defaultWalletOpt.get();
                    
                    // Update SAVINGS wallet status to DONE
                    savingsWallet.setStatus(WalletStatus.DONE);
                    walletRepository.save(savingsWallet);
                    
                    // Add maturity amount to DEFAULT wallet
                    defaultWallet.setBalance(defaultWallet.getBalance() + maturityAmount);
                    walletRepository.save(defaultWallet);
                }
            } catch (Exception e) {
                // Log error but continue processing other wallets
                System.err.println("Error processing SAVINGS wallet ID " + savingsWallet.getId() + ": " + e.getMessage());
            }
        }
    }
    
    private double calculateMaturityAmount(double principal, double annualInterestRate, int termMonths) {
        if (annualInterestRate <= 0 || termMonths <= 0) {
            return principal;
        }
        
        // Simple interest calculation: A = P * (1 + r * t) where r is annual rate and t is years
        double maturityAmount = principal * (1 + (annualInterestRate / 100.0) * (termMonths / 12.0));
        
        return Math.round(maturityAmount * 100.0) / 100.0; // Round to 2 decimal places
    }
}
