package com.epay.wallet.repository;

import com.epay.domain.wallet.entity.CurrencyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface CurrencyBalanceRepository extends JpaRepository<CurrencyBalance, Long> {

    @Query("SELECT cb FROM CurrencyBalance cb WHERE cb.wallet.id = :walletId AND UPPER(cb.currencyCode) = UPPER(:code)")
    Optional<CurrencyBalance> findByWalletIdAndCurrencyCode(
            @Param("walletId") Long walletId,
            @Param("code") String code);

    /**
     * Targeted single-row UPDATE — avoids the @ElementCollection DELETE + re-INSERT.
     * Issues: UPDATE wallet_balances SET balance = ? WHERE id = ?
     */
    @Modifying
    @Query("UPDATE CurrencyBalance cb SET cb.balance = :balance WHERE cb.id = :id")
    void updateBalance(@Param("id") Long id, @Param("balance") BigDecimal balance);

    @Modifying
    @Query("UPDATE CurrencyBalance cb SET cb.isDefault = false WHERE cb.wallet.id = :walletId")
    void clearDefaultForWallet(@Param("walletId") Long walletId);

    @Modifying
    @Query("UPDATE CurrencyBalance cb SET cb.isDefault = true WHERE cb.id = :id")
    void setDefault(@Param("id") Long id);
}
