package com.exe201.project.service.impl;

import com.exe201.project.dto.response.analysis.DeptAnalysisResponse;
import com.exe201.project.dto.response.analysis.FinanceAnalysisResponse;
import com.exe201.project.dto.response.analysis.TransactionAnalysisResponse;
import com.exe201.project.entity.Category;
import com.exe201.project.entity.Transaction;
import com.exe201.project.entity.User;
import com.exe201.project.entity.Wallets;
import com.exe201.project.enums.AnalysisType;
import com.exe201.project.enums.WalletStatus;
import com.exe201.project.enums.WalletType;
import com.exe201.project.exception.OutOfPermissionException;
import com.exe201.project.exception.ResourceNotFoundException;
import com.exe201.project.repository.TransactionRepository;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.repository.WalletRepository;
import com.exe201.project.service.AnalysisService;
import com.exe201.project.service.MembershipAccessService;
import com.exe201.project.service.UserPurchasedFeatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisServiceImpl implements AnalysisService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserPurchasedFeatureService userPurchasedFeatureService;
    private final MembershipAccessService membershipAccessService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.api.url.review}")
    private String budgetReviewInternalApiUrl;

    @Override
    @Transactional
    public Object getProfileAnalysis(Long userId, AnalysisType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        handleAdvancedAnalyticsPermission(userId);

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

        List<Transaction> transactions = transactionRepository
                .findByWalletAndTransactionTimeBetweenAndIsDeletedFalse(defaultWallet, startTime, endTime);

        double totalIncome = transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpense = Math.abs(transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(Transaction::getAmount)
                .sum());

        Double currentExpense = null;
        if (type == AnalysisType.INSTANTLY) {
            List<Transaction> currentMonthTransactions = transactionRepository
                    .findByWalletAndTransactionTimeBetweenAndIsDeletedFalse(
                            defaultWallet, currentMonthStart, LocalDateTime.now());

            currentExpense = Math.abs(currentMonthTransactions.stream()
                    .filter(t -> t.getAmount() < 0)
                    .mapToDouble(Transaction::getAmount)
                    .sum());
        }

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

        List<Wallets> debtWallets = walletRepository.findByUserAndTypeAndStatus(
                user, WalletType.DEBT, WalletStatus.ACTIVE);
        List<DeptAnalysisResponse> depts = debtWallets.stream()
                .map(wallet -> DeptAnalysisResponse.builder()
                        .name(wallet.getName())
                        .deadline(wallet.getDeadline())
                        .target(wallet.getTargetAmount())
                        .currentAmount(wallet.getBalance())
                        .build())
                .collect(Collectors.toList());

        FinanceAnalysisResponse response = FinanceAnalysisResponse.builder()
                .type(type)
                .currentLimit(defaultWallet.getTargetAmount())
                .income(totalIncome)
                .expense(-totalExpense)
                .currentExpense(currentExpense)
                .depts(depts)
                .transactions(transactionAnalysis)
                .build();

        return sendAnalysisToInternalApi(response, userId);
    }


//    private String sendAnalysisToExternalDeployedApi(FinanceAnalysisResponse analysisResponse, Long userId) {
//        try {
//            log.info("Sending analysis data to external API for user ID: {}", userId);
//
//            // First try: multipart/form-data
//            try {
//                return sendAsMultipartData(analysisResponse, userId);
//            } catch (Exception multipartException) {
//                log.warn("Multipart request failed, trying JSON request: {}", multipartException.getMessage());
//
//                // Second try: application/json
//                return sendAsJsonData(analysisResponse, userId);
//            }
//
//        } catch (Exception e) {
//            log.error("Failed to send analysis data to external API for user ID: {}. Error: {}",
//                    userId, e.getMessage(), e);
//            return "Failed to get analysis data to external API for user ID: " + userId;
//        }
//    }

//    private String sendAsMultipartData(FinanceAnalysisResponse analysisResponse, Long userId) throws Exception {
//        // Convert FinanceAnalysisResponse to JSON string
//        String jsonData = objectMapper.writeValueAsString(analysisResponse);
//        log.debug("JSON data to send (multipart): {}", jsonData);
//
//        // Create a proper file resource with Content-Type
//        byte[] jsonBytes = jsonData.getBytes("UTF-8");
//
//        // Create multipart request with correct field name
//        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
//
//        // Create HttpEntity for the file part with proper headers
//        HttpHeaders fileHeaders = new HttpHeaders();
//        fileHeaders.setContentType(MediaType.APPLICATION_JSON);
//        fileHeaders.setContentDispositionFormData("file", "budget_analysis.json");
//
//        HttpEntity<byte[]> fileEntity = new HttpEntity<>(jsonBytes, fileHeaders);
//        parts.add("file", fileEntity);
//
//        // Set main request headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        headers.set("Accept", "application/json");
//        headers.set("User-Agent", "BudgetMate-Backend/1.0");
//
//        // Create HTTP entity
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);
//
//        log.info("Sending multipart request to URL: {} with proper file headers", budgetReviewExternalApiUrl);
//
//        // Send POST request
//        ResponseEntity<String> response = restTemplate.exchange(
//                budgetReviewExternalApiUrl,
//                HttpMethod.POST,
//                requestEntity,
//                String.class
//        );
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            log.info("Successfully sent analysis data (multipart) to external API for user ID: {}. Response: {}",
//                    userId, response.getBody());
//            return response.getBody();
//        } else {
//            throw new RuntimeException("HTTP " + response.getStatusCode() + ": " + response.getBody());
//        }
//    }
//
//    private String sendAsJsonData(FinanceAnalysisResponse analysisResponse, Long userId) throws Exception {
//        // Convert FinanceAnalysisResponse to JSON string
//        String jsonData = objectMapper.writeValueAsString(analysisResponse);
//        log.debug("JSON data to send (direct): {}", jsonData);
//
//        // Set headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Accept", "application/json");
//        headers.set("User-Agent", "BudgetMate-Backend/1.0");
//
//        HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, headers);
//
//        log.info("Sending JSON request to URL: {}", budgetReviewExternalApiUrl);
//
//        // Send POST request
//        ResponseEntity<String> response = restTemplate.exchange(
//                budgetReviewExternalApiUrl,
//                HttpMethod.POST,
//                requestEntity,
//                String.class
//        );
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            log.info("Successfully sent analysis data (JSON) to external API for user ID: {}. Response: {}",
//                    userId, response.getBody());
//            return response.getBody();
//        } else {
//            throw new RuntimeException("HTTP " + response.getStatusCode() + ": " + response.getBody());
//        }
//    }

    private Object sendAnalysisToInternalApi(FinanceAnalysisResponse analysisResponse, Long userId) {
        try {
            log.info("Sending analysis data to internal API for user ID: {}", userId);

            // Create request body with 'profile' field as expected by internal API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("profile", analysisResponse);

            // Convert to JSON string
            String jsonData = objectMapper.writeValueAsString(requestBody);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "BudgetMate-Backend/1.0");

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, headers);

            log.info("Sending JSON request to internal API URL: {}", budgetReviewInternalApiUrl);

            // Send POST request
            ResponseEntity<String> response = restTemplate.exchange(
                    budgetReviewInternalApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully sent analysis data to internal API for user ID: {}. Response: {}",
                        userId, response.getBody());
                try {
                    return objectMapper.readValue(response.getBody(), Object.class);
                } catch (Exception parseException) {
                    log.warn("Failed to parse JSON response, returning raw string: {}", parseException.getMessage());
                    return response.getBody();
                }
                
            } else {
                log.warn("Internal API returned non-success status for user ID: {}. Status: {}, Response: {}",
                        userId, response.getStatusCode(), response.getBody());
                return "Internal API error: " + response.getStatusCode();
            }

        } catch (Exception e) {
            log.error("Failed to send analysis data to internal API for user ID: {}. Error: {}",
                    userId, e.getMessage(), e);
            return "Failed to get analysis data to internal API for user ID: " + userId;
        }
    }

    private void handleAdvancedAnalyticsPermission(Long userId) {
        boolean hasMembershipAccess = membershipAccessService.hasAdvancedAnalytics(userId);

        if (hasMembershipAccess) {
            return;
        }

        boolean hasRemainingUsage = userPurchasedFeatureService.hasRemainingUsage(userId, "ADVANCED_ANALYTICS");

        if (hasRemainingUsage) {
            userPurchasedFeatureService.consumeFeatureUsage(userId, "ADVANCED_ANALYTICS");
        } else {
            throw new OutOfPermissionException("You don't have access to advanced analytics");
        }
    }
} 