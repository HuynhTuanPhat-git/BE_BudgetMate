package com.exe201.project.service.impl;

import com.exe201.project.dto.request.WalletRequest;
import com.exe201.project.dto.response.WalletResponse;
import com.exe201.project.entity.User;
import com.exe201.project.entity.Wallets;
import com.exe201.project.enums.WalletStatus;
import com.exe201.project.enums.WalletType;
import com.exe201.project.exception.OutOfPermissionException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.exception.WrongTypeException;
import com.exe201.project.mapper.WalletMapper;
import com.exe201.project.repository.WalletRepository;
import com.exe201.project.service.MembershipAccessService;
import com.exe201.project.service.UserPurchasedFeatureService;
import com.exe201.project.service.UserService;
import com.exe201.project.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WalletServiceImpl implements WalletService {
    
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final UserService userService;
    private final MembershipAccessService membershipAccessService;
    private final UserPurchasedFeatureService userPurchasedFeatureService;

    @Override
    public WalletResponse createWallet(WalletRequest request) {
        User user = userService.getAuthenticatedUser();
        List<Wallets> existingWallets = walletRepository.findAllByUserId(user.getId());
        handleWalletCreationPermissions(user.getId(), request.type(), existingWallets);

        Wallets wallet = new Wallets();
        wallet.setType(request.type());
        wallet.setName(request.name());
        wallet.setBalance(0.0);
        wallet.setTargetAmount(request.targetAmount());
        wallet.setInterestRate(request.interestRate() == 0.0 ? 0.0 : request.interestRate());
        wallet.setDeadline(request.deadline());

        if (request.type().equals(WalletType.SAVINGS)) {
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

            wallet.setDeadline(request.startDate().plusMonths(request.termMonths()));
            wallet.setStatus(WalletStatus.ACTIVE);
        }

        if (request.type().equals(WalletType.DEFAULT)) {
            if ((request.interestRate() != 0) ||
                request.deadline() != null ||
                request.startDate() != null ||
                request.termMonths() != null) {
                throw new WrongTypeException("DEFAULT wallet must not have interest rate, deadline, start date, or term months");
            }
            wallet.setStatus(WalletStatus.ACTIVE);
        }

        if (request.type().equals(WalletType.DEBT)) {
            if (request.deadline() == null) {
                throw new IllegalArgumentException("Deadline is required for DEBT wallet");
            }
            wallet.setStatus(WalletStatus.ACTIVE);
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
            throw new WrongTypeException("Cannot change wallet type.");
        }
        
        // Validate update based on wallet type
        switch (wallet.getType()) {
            case DEFAULT:
                // DEFAULT wallet restrictions
                if ((request.interestRate() != 0) ||
                    request.deadline() != null ||
                    request.startDate() != null ||
                    request.termMonths() != null) {
                    throw new WrongTypeException("DEFAULT wallet must not have interest rate, deadline, start date, or term months");
                }
                break;
                
            case SAVINGS:
                // SAVINGS wallet validation
                if (request.interestRate() <= 0) {
                    throw new IllegalArgumentException("Interest rate must be greater than 0 for SAVINGS wallet");
                }
                if (request.startDate() == null) {
                    throw new IllegalArgumentException("Start date is required for SAVINGS wallet");
                }
                if (request.termMonths() == null || request.termMonths() < 1) {
                    throw new IllegalArgumentException("Term months must be at least 1 for SAVINGS wallet");
                }
                // Update SAVINGS specific fields
                wallet.setStartDate(request.startDate());
                wallet.setTermMonths(request.termMonths());
                // Recalculate deadline based on start date and term
                wallet.setDeadline(request.startDate().plusMonths(request.termMonths()));
                break;
                
            case DEBT:
                // DEBT wallet validation
                if (request.deadline() == null) {
                    throw new IllegalArgumentException("Deadline is required for DEBT wallet");
                }
                wallet.setDeadline(request.deadline());
                break;
        }
        
        // Update common fields
        wallet.setName(request.name());
        wallet.setTargetAmount(request.targetAmount());
        
        // Only update interest rate if it's not a DEFAULT wallet
        if (!wallet.getType().equals(WalletType.DEFAULT)) {
            wallet.setInterestRate(request.interestRate());
        }

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
    
    /**
     * Helper method to get wallet count by type for a user
     */
    private long getWalletCountByType(List<Wallets> wallets, WalletType type) {
        return wallets.stream()
                .filter(wallet -> wallet.getType().equals(type))
                .count();
    }
    
    /**
     * Helper method to validate wallet creation limits
     */
    private void validateWalletCreationLimits(Long userId, WalletType type, List<Wallets> existingWallets) {
        switch (type) {
            case DEFAULT:
                if (getWalletCountByType(existingWallets, WalletType.DEFAULT) >= 1) {
                    throw new OutOfPermissionException("You can only have 1 DEFAULT wallet per account");
                }
                break;

            case SAVINGS:
                Integer totalLimit = getTotalWalletLimit(userId, "CREATE_SAVINGS_WALLETS");
                long savingsCount = getWalletCountByType(existingWallets, WalletType.SAVINGS);

                if (totalLimit != null && savingsCount >= totalLimit) {
                    throw new OutOfPermissionException(
                            String.format("You have reached the absolute maximum number of SAVINGS wallets (%d)", totalLimit)
                    );
                }
                break;

            case DEBT:
                Integer deptTotalLimit = getTotalWalletLimit(userId, "CREATE_DEPT_WALLETS");
                long deptCount = getWalletCountByType(existingWallets, WalletType.DEBT);

                if (deptTotalLimit != null && deptCount >= deptTotalLimit) {
                    throw new OutOfPermissionException(
                            String.format("You have reached the absolute maximum number of DEBT wallets (%d)", deptTotalLimit)
                    );
                }
                break;
        }
    }

    private Integer getTotalWalletLimit(Long userId, String featureKey) {
        Integer membershipLimit = membershipAccessService.getMembershipPlanLimit(userId, featureKey);
        Integer purchasedUsage = userPurchasedFeatureService.getRemainingUsage(userId, featureKey);

        if (membershipLimit == null) {
            return null;
        }

        return membershipLimit + (purchasedUsage != null ? purchasedUsage : 0);
    }

    private void handleWalletCreationPermissions(Long userId, WalletType walletType, List<Wallets> existingWallets) {
        String featureKey = mapWalletTypeToFeatureKey(walletType);

        if (featureKey == null) {
            return;
        }

        long currentCount = getWalletCountByType(existingWallets, walletType);

        Integer membershipPlanLimit = membershipAccessService.getMembershipPlanLimit(userId, featureKey);

        if (membershipPlanLimit == null) {
            return;
        }

        if (currentCount < membershipPlanLimit) {
            return;
        }

        if (userPurchasedFeatureService.hasRemainingUsage(userId, featureKey)) {
            userPurchasedFeatureService.consumeFeatureUsage(userId, featureKey);
        } else {
            throw new OutOfPermissionException(
                    String.format("You have reached the maximum number of %s wallets (%d) allowed for your membership plan",
                            walletType.name(), membershipPlanLimit));
        }
    }

    private String mapWalletTypeToFeatureKey(WalletType walletType) {
        return switch (walletType) {
            case SAVINGS -> "CREATE_SAVINGS_WALLETS";
            case DEBT -> "CREATE_DEPT_WALLETS";
            case DEFAULT -> null;
        };
    }
}
