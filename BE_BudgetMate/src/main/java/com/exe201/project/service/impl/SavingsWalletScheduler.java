package com.exe201.project.service.impl;

import com.exe201.project.entity.Category;
import com.exe201.project.entity.Transaction;
import com.exe201.project.entity.User;
import com.exe201.project.entity.Wallets;
import com.exe201.project.enums.WalletStatus;
import com.exe201.project.enums.WalletType;
import com.exe201.project.repository.CategoryRepository;
import com.exe201.project.repository.TransactionRepository;
import com.exe201.project.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SavingsWalletScheduler {
    
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    
    /**
     * Scheduled task that runs daily at midnight to process expired SAVINGS wallets
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    public void processExpiredSavingsWallets() {
        log.info("Starting daily check for expired SAVINGS wallets");
        
        LocalDate currentDate = LocalDate.now();
        List<Wallets> expiredSavingsWallets = walletRepository.findExpiredSavingsWallets(currentDate);
        
        if (expiredSavingsWallets.isEmpty()) {
            log.info("No expired SAVINGS wallets found");
            return;
        }
        
        log.info("Found {} expired SAVINGS wallets to process", expiredSavingsWallets.size());
        
        for (Wallets savingsWallet : expiredSavingsWallets) {
            try {
                processExpiredSavingsWallet(savingsWallet);
            } catch (Exception e) {
                log.error("Error processing expired SAVINGS wallet ID {}: {}", 
                         savingsWallet.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Completed processing expired SAVINGS wallets");
    }
    
    /**
     * Process a single expired SAVINGS wallet
     */
    private void processExpiredSavingsWallet(Wallets savingsWallet) {
        log.info("Processing expired SAVINGS wallet ID {} for user ID {}", 
                savingsWallet.getId(), savingsWallet.getUser().getId());
        
        // Calculate maturity amount
        double maturityAmount = calculateMaturityAmount(
            savingsWallet.getTargetAmount(),
            savingsWallet.getInterestRate(),
            savingsWallet.getTermMonths()
        );
        
        log.info("SAVINGS wallet ID {} - Principal: {}, Interest Rate: {}%, Term: {} months, Maturity Amount: {}",
                savingsWallet.getId(), savingsWallet.getTargetAmount(), 
                savingsWallet.getInterestRate(), savingsWallet.getTermMonths(), maturityAmount);
        
        // Find user's DEFAULT wallet
        User user = savingsWallet.getUser();
        Optional<Wallets> defaultWalletOpt = walletRepository.findByUserAndType(user, WalletType.DEFAULT);
        
        if (defaultWalletOpt.isEmpty()) {
            log.error("No DEFAULT wallet found for user ID {} - cannot transfer matured savings", user.getId());
            return;
        }
        
        Wallets defaultWallet = defaultWalletOpt.get();
        
        // Update SAVINGS wallet status to DONE
        savingsWallet.setStatus(WalletStatus.DONE);
        walletRepository.save(savingsWallet);
        
        // Add maturity amount to DEFAULT wallet
        defaultWallet.setBalance(defaultWallet.getBalance() + maturityAmount);
        walletRepository.save(defaultWallet);
        
        // Create transaction in DEFAULT wallet
        createMaturityTransaction(defaultWallet, maturityAmount, savingsWallet.getName());
        
        log.info("Successfully processed SAVINGS wallet ID {} - transferred {} to DEFAULT wallet", 
                savingsWallet.getId(), maturityAmount);
    }
    
    /**
     * Calculate maturity amount using compound interest formula
     * A = P * (1 + r/100)^(t/12) where t is in months
     */
    private double calculateMaturityAmount(double principal, double annualInterestRate, int termMonths) {
        if (annualInterestRate <= 0 || termMonths <= 0) {
            return principal;
        }
        
        // Convert annual rate to decimal and adjust for monthly compounding
        double monthlyRate = annualInterestRate / 100.0 / 12.0;
        
        // Simple interest calculation for now (can be changed to compound interest if needed)
        // A = P * (1 + r * t) where r is monthly rate and t is months
        double maturityAmount = principal * (1 + (annualInterestRate / 100.0) * (termMonths / 12.0));
        
        return Math.round(maturityAmount * 100.0) / 100.0; // Round to 2 decimal places
    }
    
    /**
     * Create a transaction recording the maturity of SAVINGS wallet
     */
    private void createMaturityTransaction(Wallets defaultWallet, double amount, String savingsWalletName) {
        // Find or create "savings" category
        Category savingsCategory = categoryRepository.findByName("savings")
            .orElseGet(() -> {
                Category category = new Category();
                category.setName("savings");
                category.setDescription("Savings maturity transfers");
                return categoryRepository.save(category);
            });
        
        Transaction transaction = new Transaction();
        transaction.setWallet(defaultWallet);
        transaction.setAmount(amount);
        transaction.setDescription("Saving from " + savingsWalletName);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setCategory(savingsCategory);
        transaction.setDeleted(false);
        
        transactionRepository.save(transaction);
        
        log.info("Created maturity transaction of {} for DEFAULT wallet from SAVINGS wallet '{}'", 
                amount, savingsWalletName);
    }
}
