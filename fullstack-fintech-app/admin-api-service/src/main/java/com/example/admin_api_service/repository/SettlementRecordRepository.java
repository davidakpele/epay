package com.example.admin_api_service.repository;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.admin_api_service.enums.SettlementRecordStatus;
import com.example.admin_api_service.models.settlementsAndReconciliation.SettlementRecord;

@Repository
public interface SettlementRecordRepository extends JpaRepository<SettlementRecord, String> {
    Page<SettlementRecord> findAllBySettlementBatchId(String batchId, Pageable pageable);
    Page<SettlementRecord> findAllBySettlementBatchIdAndStatus(String batchId, SettlementRecordStatus status, Pageable pageable);
    List<SettlementRecord> findAllBySettlementBatchIdAndStatus(String batchId, SettlementRecordStatus status);
    Page<SettlementRecord> findAllByWalletId(Long walletId, Pageable pageable);
 
    @Query("SELECT SUM(CASE WHEN r.recordType = 'CREDIT' THEN r.grossAmount ELSE 0 END), " +
           "SUM(CASE WHEN r.recordType = 'DEBIT' THEN r.grossAmount ELSE 0 END) " +
           "FROM SettlementRecord r " +
           "WHERE r.currency = :currency " +
           "AND r.status = 'SETTLED' " +
           "AND r.settledAt BETWEEN :start AND :end")
    BigDecimal[] sumTotalsByCurrencyAndPeriod(@Param("currency") String currency,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
 
    @Query("SELECT COUNT(r) FROM SettlementRecord r " +
           "WHERE r.currency = :currency " +
           "AND r.status = 'SETTLED' " +
           "AND r.settledAt BETWEEN :start AND :end")
    int countByCurrencyAndPeriod(@Param("currency") String currency,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}