package com.exe201.project.service;

import com.exe201.project.dto.request.WalletRequest;
import com.exe201.project.dto.response.WalletResponse;

import java.util.List;

public interface WalletService {
    WalletResponse createWallet(WalletRequest request);
    List<WalletResponse> getAllWallet();
    WalletResponse getDetailWallet(String walletId);
    WalletResponse updateWallet(String walletId, WalletRequest request);
    void deleteWallet(String walletId);
    List<WalletResponse> getWalletsByUserId(Long userId);
    Double getTotalBalance();
    Double getTotalBalanceByType(String type);
}
