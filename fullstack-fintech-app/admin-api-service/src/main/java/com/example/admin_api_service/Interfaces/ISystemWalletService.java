package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import java.util.List;
import com.example.admin_api_service.enums.Currency;
import com.example.admin_api_service.enums.WalletStatus;
import com.example.admin_api_service.models.LiquidityTransaction;
import com.example.admin_api_service.models.SystemWallet;
import com.example.admin_api_service.payloads.FundWalletRequest;
import com.example.admin_api_service.payloads.RebalanceRequest;
import com.example.admin_api_service.payloads.UpdateThresholdRequest;
import com.example.admin_api_service.payloads.WithdrawWalletRequest;
import com.example.admin_api_service.responses.DashboardSummaryResponse;

public interface ISystemWalletService {
    SystemWallet provisionWallet(Currency currency, BigDecimal minimumThreshold);

    LiquidityTransaction fundWallet(FundWalletRequest request, Long adminId);

    LiquidityTransaction withdrawFromWallet(WithdrawWalletRequest request, Long adminId);

    void rebalance(RebalanceRequest request, Long adminId);

    void reserveBalance(Currency currency, BigDecimal amount, String txnRef);

    void releaseReserve(Currency currency, BigDecimal amount, String txnRef);

    void settleReserve(Currency currency, BigDecimal amount, String txnRef);

    SystemWallet updateThreshold(UpdateThresholdRequest request, Long adminId);

    SystemWallet setWalletStatus(Currency currency, WalletStatus newStatus, Long adminId);

    void syncUserLiabilities(Currency currency, BigDecimal totalLiabilities);

    SystemWallet getWalletByCurrency(Currency currency);

    List<SystemWallet> getAllWallets();

    List<LiquidityTransaction> getTransactionHistory(Currency currency);

    DashboardSummaryResponse buildDashboardSummary(long totalUsers, long totalHistory, long totalVirtualCards);
}
