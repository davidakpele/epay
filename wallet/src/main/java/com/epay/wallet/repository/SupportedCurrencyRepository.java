package com.epay.wallet.repository;

import com.epay.wallet.domain.entity.SupportedCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportedCurrencyRepository extends JpaRepository<SupportedCurrency, Long> {

    Optional<SupportedCurrency> findByCodeIgnoreCase(String code);

    List<SupportedCurrency> findByActiveTrue();

    List<SupportedCurrency> findByDefaultEligibleTrue();

    boolean existsByCodeIgnoreCase(String code);

    @Modifying
    @Query("UPDATE SupportedCurrency c SET c.active = :active, c.lastUpdatedBy = :adminId WHERE c.id = :id")
    void setActive(@Param("id") Long id,
                   @Param("active") boolean active,
                   @Param("adminId") Long adminId);

    @Modifying
    @Query("UPDATE SupportedCurrency c SET c.exchangeRate = :rate, c.lastUpdatedBy = :adminId WHERE c.code = :code")
    void updateExchangeRate(@Param("code") String code,
                            @Param("rate") BigDecimal rate,
                            @Param("adminId") Long adminId);
}
