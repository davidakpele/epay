package com.example.auth_user_service.interfaces;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.example.auth_user_service.dtos.WalletSectionDTO;
import com.example.auth_user_service.wallet.grpc.GetWalletByCurrencyRequest;
import com.example.auth_user_service.wallet.grpc.WalletBalanceResponse;
import com.example.auth_user_service.wallet.grpc.WalletDeductionRequest;
import com.example.auth_user_service.wallet.grpc.WithdrawResponse;

public interface IWalletServiceClient {
    CompletableFuture<Map<String, Object>> createUserWallet(Long userId);
    WalletSectionDTO getWalletSectionByUser(Long userId);
    WalletBalanceResponse getWalletByCurrency(GetWalletByCurrencyRequest request);
    WithdrawResponse walletDeduct(WalletDeductionRequest payloads);
    void onWebSocketMessage(String messageJson);
}