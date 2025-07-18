package com.exe201.project.service.impl;


import com.exe201.project.dto.request.TransactionRequest;
import com.exe201.project.dto.request.TransactionSummary;
import com.exe201.project.dto.response.TransactionResponse;
import com.exe201.project.entity.Category;
import com.exe201.project.entity.Transaction;
import com.exe201.project.entity.User;
import com.exe201.project.entity.Wallets;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.mapper.TransactionMapper;
import com.exe201.project.repository.CategoryRepository;
import com.exe201.project.repository.TransactionRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.repository.WalletRepository;
import com.exe201.project.service.TransactionService;
import com.exe201.project.service.UserService;
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
public class TransactionServiceImpl implements TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final UserService userService;

    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {
        // Get current user
        User user = userService.getAuthenticatedUser();

        // Verify wallet belongs to user
        Wallets wallet = walletRepository.findByIdAndUserId(request.walletId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setTransactionTime(request.transactionTime() != null ? request.transactionTime() : LocalDateTime.now());
        transaction.setWallet(wallet);
        transaction.setCategory(category);

        // Update wallet balance
        wallet.setBalance(wallet.getBalance() + request.amount());
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        walletRepository.save(wallet);
        
        return transactionMapper.toTransactionResponse(savedTransaction);
    }

    @Override
    public List<TransactionResponse> getTransactionsByWallet(Long walletId) {
        // Get current user
        User user = userService.getAuthenticatedUser();

        // Verify wallet belongs to user
        walletRepository.findByIdAndUserId(walletId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        List<Transaction> transactions = transactionRepository.findByWalletIdAndIsDeletedFalseOrderByTransactionTimeDesc(walletId);
        return transactions.stream()
                .map(transactionMapper::toTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getTransactionsByDateRange(Long walletId, LocalDateTime startDate, LocalDateTime endDate) {
        // Get current user
        User user = userService.getAuthenticatedUser();

        // Verify wallet belongs to user
        walletRepository.findByIdAndUserId(walletId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        List<Transaction> transactions = transactionRepository.findByWalletIdAndDateRange(walletId, startDate, endDate);
        return transactions.stream()
                .map(transactionMapper::toTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Verify transaction belongs to current user's wallet
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!transaction.getWallet().getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        return transactionMapper.toTransactionResponse(transaction);
    }

    @Override
    public TransactionResponse updateTransaction(Long transactionId, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Verify transaction belongs to current user's wallet
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!transaction.getWallet().getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        // Check if transaction is already deleted
        if (transaction.isDeleted()) {
            throw new ResourceNotFoundException("Cannot update deleted transaction");
        }

        // Store original values for audit trail
        if (transaction.getOriginalAmount() == null) {
            transaction.setOriginalAmount(transaction.getAmount());
            transaction.setOriginalDescription(transaction.getDescription());
            transaction.setOriginalTransactionTime(transaction.getTransactionTime());
        }

        // Update wallet balance by reversing old amount and adding new amount
        Wallets wallet = transaction.getWallet();
        wallet.setBalance(wallet.getBalance() - transaction.getAmount() + request.amount());

        // Update transaction directly
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setTransactionTime(request.transactionTime() != null ? request.transactionTime() : transaction.getTransactionTime());
        
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            transaction.setCategory(category);
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);
        walletRepository.save(wallet);
        
        return transactionMapper.toTransactionResponse(updatedTransaction);
    }

    @Override
    public void deleteTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Verify transaction belongs to current user's wallet
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!transaction.getWallet().getUser().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        // Check if transaction is already deleted
        if (transaction.isDeleted()) {
            throw new ResourceNotFoundException("Transaction is already deleted");
        }

        // Store original values for audit trail before soft delete
        transaction.setOriginalAmount(transaction.getAmount());
        transaction.setOriginalDescription(transaction.getDescription());
        transaction.setOriginalTransactionTime(transaction.getTransactionTime());

        // Update wallet balance by reversing the transaction
        Wallets wallet = transaction.getWallet();
        wallet.setBalance(wallet.getBalance() - transaction.getAmount());

        // Soft delete: set isDeleted to true
        transaction.setDeleted(true);

        walletRepository.save(wallet);
        transactionRepository.save(transaction);
    }

    @Override
    public TransactionSummary getTransactionSummary(Long walletId) {
        // Get current user
        User user = userService.getAuthenticatedUser();

        // Verify wallet belongs to user
        walletRepository.findByIdAndUserId(walletId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        Double totalIncome = transactionRepository.getTotalIncomeByWalletId(walletId);
        Double totalExpense = transactionRepository.getTotalExpenseByWalletId(walletId);
        Long transactionCount = (long) transactionRepository.findByWalletIdAndIsDeletedFalse(walletId).size();

        totalIncome = totalIncome != null ? totalIncome : 0.0;
        totalExpense = totalExpense != null ? Math.abs(totalExpense) : 0.0;
        Double netAmount = totalIncome - totalExpense;

        return new TransactionSummary(totalIncome, totalExpense, netAmount, transactionCount);
    }

    @Override
    public List<TransactionResponse> getDeletedTransactions(Long walletId) {
        // Get current user
        User user = userService.getAuthenticatedUser();

        // Verify wallet belongs to user
        walletRepository.findByIdAndUserId(walletId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        List<Transaction> deletedTransactions = transactionRepository.findByWalletIdAndIsDeletedTrueOrderByUpdatedAtDesc(walletId);
        return deletedTransactions.stream()
                .map(transactionMapper::toTransactionResponse)
                .collect(Collectors.toList());
    }
}
