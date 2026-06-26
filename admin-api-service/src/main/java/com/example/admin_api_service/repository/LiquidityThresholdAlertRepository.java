package com.example.admin_api_service.repository;

import com.example.admin_api_service.models.LiquidityThresholdAlert;
import com.example.admin_api_service.models.SystemWallet;
import com.example.admin_api_service.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiquidityThresholdAlertRepository extends JpaRepository<LiquidityThresholdAlert, String> {

    List<LiquidityThresholdAlert> findBySystemWallet(SystemWallet systemWallet);

    List<LiquidityThresholdAlert> findBySystemWalletId(String systemWalletId);

    List<LiquidityThresholdAlert> findByStatus(AlertStatus status);

    List<LiquidityThresholdAlert> findBySystemWalletIdAndStatus(String systemWalletId, AlertStatus status);
}
