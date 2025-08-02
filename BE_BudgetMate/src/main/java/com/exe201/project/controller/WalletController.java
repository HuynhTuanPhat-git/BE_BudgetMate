package com.exe201.project.controller;

import com.exe201.project.dto.request.WalletRequest;
import com.exe201.project.dto.response.ApiResponse;
import com.exe201.project.dto.response.WalletResponse;
import com.exe201.project.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@Valid @RequestBody WalletRequest request){
        WalletResponse wallet = walletService.createWallet(request);
        return ResponseEntity.ok(
                ApiResponse.<WalletResponse>builder()
                        .message("Wallet created successfully.")
                        .data(wallet)
                        .build()
        );
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<?>> getAllWallet(){
        List<WalletResponse> wallet = walletService.getAllWallet();
        return ResponseEntity.ok(
                ApiResponse.<List<WalletResponse>>builder()
                        .message("All wallet retrieved successfully.")
                        .data(wallet)
                        .build()
        );
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletById(@PathVariable String walletId){
        WalletResponse wallet = walletService.getDetailWallet(walletId);
        return ResponseEntity.ok(
                ApiResponse.<WalletResponse>builder()
                        .message("Wallet data retrieved successfully.")
                        .data(wallet)
                        .build()
        );
    }

    @PutMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> updateWallet(@PathVariable String walletId,
                                                                    @RequestBody @Valid WalletRequest request){
        WalletResponse wallet = walletService.updateWallet(walletId, request);
        return ResponseEntity.ok(
                ApiResponse.<WalletResponse>builder()
                        .message("Wallet data updated successfully.")
                        .data(wallet)
                        .build()
        );
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> deleteWallet(@PathVariable String walletId){
        walletService.deleteWallet(walletId);
        return ResponseEntity.ok(
                ApiResponse.<WalletResponse>builder()
                        .message("Wallet deleted successfully.")
                        .build()
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getWalletsByUserId(@PathVariable Long userId) {
        List<WalletResponse> wallets = walletService.getWalletsByUserId(userId);
        return ResponseEntity.ok(
                ApiResponse.<List<WalletResponse>>builder()
                        .message("User wallets retrieved successfully.")
                        .data(wallets)
                        .build()
        );
    }

    @GetMapping("/total-balance")
    public ResponseEntity<ApiResponse<Double>> getTotalBalance() {
        Double totalBalance = walletService.getTotalBalance();

        return ResponseEntity.ok(
                ApiResponse.<Double>builder()
                        .message("Total balance retrieved successfully.")
                        .data(totalBalance)
                        .build()
        );
    }

    @GetMapping("/total-balance/{walletType}")
    public ResponseEntity<ApiResponse<Double>> getTotalBalanceByType(@PathVariable String walletType) {
        Double totalBalance = walletService.getTotalBalanceByType(walletType);

        return ResponseEntity.ok(
                ApiResponse.<Double>builder()
                        .message("Total balance by type retrieved successfully.")
                        .data(totalBalance)
                        .build()
        );
    }

    @PostMapping("/process-expired-savings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> processExpiredSavingsWallets() {
        walletService.processExpiredSavingsWallets();
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .message("Expired SAVINGS wallets processed successfully.")
                        .data("Check wallet balances and transaction history for updates")
                        .build()
        );
    }
}
