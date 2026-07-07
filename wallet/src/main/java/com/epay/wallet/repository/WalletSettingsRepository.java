package com.epay.wallet.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.epay.domain.wallet.entity.WalletSettings;

@Repository
public interface WalletSettingsRepository extends JpaRepository<WalletSettings, Long> {

    @Query("SELECT ws FROM WalletSettings ws WHERE ws.wallet.id = :walletId")
    Optional<WalletSettings> findByWalletId(@Param("walletId") Long walletId);

}