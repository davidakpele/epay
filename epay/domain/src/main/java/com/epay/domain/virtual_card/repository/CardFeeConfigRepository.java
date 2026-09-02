package com.epay.domain.virtual_card.repository;

import com.epay.domain.virtual_card.entity.CardFeeConfig;
import com.epay.domain.virtual_card.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardFeeConfigRepository extends JpaRepository<CardFeeConfig, Long> {
    Optional<CardFeeConfig> findByCurrencyCodeIgnoreCaseAndCardType(String currencyCode, CardType cardType);

    List<CardFeeConfig> findByCurrencyCodeIgnoreCase(String currencyCode);

    List<CardFeeConfig> findByActiveTrue();

    List<CardFeeConfig> findAllByOrderByCurrencyCodeAscCardTypeAsc();

    boolean existsByCurrencyCodeIgnoreCaseAndCardType(String currencyCode, CardType cardType);

    @Modifying
    @Query("""
           UPDATE CardFeeConfig c
              SET c.feeAmount = :amount,
                  c.updatedBy = :adminId
            WHERE c.id = :id
           """)
    void updateFeeAmount(@Param("id") Long id,
                         @Param("amount") BigDecimal amount,
                         @Param("adminId") Long adminId);

    @Modifying
    @Query("""
           UPDATE CardFeeConfig c
              SET c.active = :active,
                  c.updatedBy = :adminId
            WHERE c.id = :id
           """)
    void setActive(@Param("id") Long id,
                   @Param("active") boolean active,
                   @Param("adminId") Long adminId);
}
