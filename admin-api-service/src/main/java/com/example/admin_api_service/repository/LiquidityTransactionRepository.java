package com.example.admin_api_service.repository;

import com.example.admin_api_service.models.LiquidityTransaction;
import com.example.admin_api_service.models.SystemWallet;
import com.example.admin_api_service.enums.LiquidityTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LiquidityTransactionRepository extends JpaRepository<LiquidityTransaction, String> {

    Optional<LiquidityTransaction> findByReference(String reference);

    List<LiquidityTransaction> findBySystemWallet(SystemWallet systemWallet);

    List<LiquidityTransaction> findBySystemWalletId(String systemWalletId);

    List<LiquidityTransaction> findByType(LiquidityTransactionType type);
}