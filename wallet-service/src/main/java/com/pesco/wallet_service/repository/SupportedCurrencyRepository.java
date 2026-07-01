package com.pesco.wallet_service.repository;

import com.pesco.wallet_service.models.SupportedCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportedCurrencyRepository extends JpaRepository<SupportedCurrency, Long> {

    Optional<SupportedCurrency> findByCodeIgnoreCase(String code);

    /** All currencies that are currently switched on. */
    @Query("SELECT c FROM SupportedCurrency c WHERE c.enabled = true ORDER BY c.code ASC")
    List<SupportedCurrency> findAllEnabled();

    /** All currencies regardless of enabled flag. */
    @Query("SELECT c FROM SupportedCurrency c ORDER BY c.code ASC")
    List<SupportedCurrency> findAllOrdered();

    boolean existsByCodeIgnoreCase(String code);
}
