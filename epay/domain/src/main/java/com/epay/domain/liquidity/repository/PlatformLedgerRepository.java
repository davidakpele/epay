package com.epay.domain.liquidity.repository;

import com.epay.domain.liquidity.entity.PlatformLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface PlatformLedgerRepository extends JpaRepository<PlatformLedger, Long> {

    Page<PlatformLedger> findByGatewayOrderByCreatedAtDesc(String gateway, Pageable pageable);

    Page<PlatformLedger> findByEntryTypeOrderByCreatedAtDesc(String entryType, Pageable pageable);

    boolean existsByReference(String reference);

    @Query("SELECT COALESCE(SUM(l.amount),0) FROM PlatformLedger l WHERE l.gateway = :gw AND l.entryType = :type AND l.createdAt >= :from")
    BigDecimal sumByGatewayAndTypeAfter(
            @Param("gw") String gateway,
            @Param("type") String entryType,
            @Param("from") LocalDateTime from);

    @Query("SELECT l FROM PlatformLedger l WHERE l.gateway = :gw ORDER BY l.createdAt DESC")
    Page<PlatformLedger> findLatestByGateway(@Param("gw") String gateway, Pageable pageable);
}
