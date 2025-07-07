package com.exe201.project.service.impl;

import com.exe201.project.dto.response.analysis.DeptAnalysisResponse;
import com.exe201.project.dto.response.analysis.FinanceAnalysisResponse;
import com.exe201.project.dto.response.analysis.TransactionAnalysisResponse;
import com.exe201.project.entity.Category;
import com.exe201.project.entity.Transaction;
import com.exe201.project.entity.User;
import com.exe201.project.entity.Wallets;
import com.exe201.project.enums.AnalysisType;
import com.exe201.project.enums.WalletType;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.repository.TransactionRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.repository.WalletRepository;
import com.exe201.project.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public FinanceAnalysisResponse getProfileAnalysis(Long userId, AnalysisType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Lấy ví DEFAULT của user
        Wallets defaultWallet = walletRepository.findByUserAndType(user, WalletType.DEFAULT)
                .orElseThrow(() -> new ResourceNotFoundException("Default wallet not found"));

        LocalDateTime startTime;
        LocalDateTime endTime;
        LocalDateTime currentMonthStart;
        
        if (type == AnalysisType.MONTHLY) {
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            startTime = lastMonth.atDay(1).atStartOfDay();
            endTime = lastMonth.atEndOfMonth().atTime(23, 59, 59);
            currentMonthStart = null;
        } else {
            YearMonth lastMonth = YearMonth.now().minusMonths(1);
            startTime = lastMonth.atDay(1).atStartOfDay();
            endTime = LocalDateTime.now();
            currentMonthStart = YearMonth.now().atDay(1).atStartOfDay();
        }

        // Lấy transactions theo thời gian
        List<Transaction> transactions = transactionRepository
                .findByWalletAndTransactionTimeBetweenAndIsDeletedFalse(defaultWallet, startTime, endTime);

        // Tính toán income và expense
        double totalIncome = transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpense = Math.abs(transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(Transaction::getAmount)
                .sum());

        // Tính currentExpense cho INSTANTLY
        Double currentExpense = null;
        if (type == AnalysisType.INSTANTLY && currentMonthStart != null) {
            List<Transaction> currentMonthTransactions = transactionRepository
                    .findByWalletAndTransactionTimeBetweenAndIsDeletedFalse(
                            defaultWallet, currentMonthStart, LocalDateTime.now());
            
            currentExpense = Math.abs(currentMonthTransactions.stream()
                    .filter(t -> t.getAmount() < 0)
                    .mapToDouble(Transaction::getAmount)
                    .sum());
        }

        // Tính toán transactions theo category
        Map<Category, List<Transaction>> transactionsByCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory));

        List<TransactionAnalysisResponse> transactionAnalysis = transactionsByCategory.entrySet().stream()
                .map(entry -> {
                    Category category = entry.getKey();
                    List<Transaction> categoryTransactions = entry.getValue();
                    
                    double categoryIncome = categoryTransactions.stream()
                            .filter(t -> t.getAmount() > 0)
                            .mapToDouble(Transaction::getAmount)
                            .sum();
                    
                    double categoryExpense = Math.abs(categoryTransactions.stream()
                            .filter(t -> t.getAmount() < 0)
                            .mapToDouble(Transaction::getAmount)
                            .sum());
                    
                    return TransactionAnalysisResponse.builder()
                            .totalIncome(categoryIncome)
                            .totalExpense(-categoryExpense)
                            .category(category.getName())
                            .build();
                })
                .collect(Collectors.toList());

        // Lấy các ví nợ (DEBT wallets)
        Optional<Wallets> debtWallets = walletRepository.findByUserAndType(user, WalletType.DEBT);
        List<DeptAnalysisResponse> depts = debtWallets.stream()
                .map(wallet -> DeptAnalysisResponse.builder()
                        .name(wallet.getName())
                        .deadline(wallet.getDeadline())
                        .target(wallet.getTargetAmount())
                        .currentAmount(wallet.getBalance())
                        .build())
                .collect(Collectors.toList());

        return FinanceAnalysisResponse.builder()
                .type(type)
                .currentLimit(defaultWallet.getTargetAmount())
                .income(totalIncome)
                .expense(-totalExpense)
                .currentExpense(currentExpense)
                .depts(depts)
                .transactions(transactionAnalysis)
                .build();
    }
} 