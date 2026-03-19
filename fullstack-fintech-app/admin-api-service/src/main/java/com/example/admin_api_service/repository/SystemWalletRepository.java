package com.example.admin_api_service.repository;

import com.example.admin_api_service.models.SystemWallet;
import com.example.admin_api_service.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemWalletRepository extends JpaRepository<SystemWallet, String> {

    Optional<SystemWallet> findByCurrency(Currency currency);

    Optional<SystemWallet> findByWalletReference(String walletReference);

    boolean existsByCurrency(Currency currency);
}
