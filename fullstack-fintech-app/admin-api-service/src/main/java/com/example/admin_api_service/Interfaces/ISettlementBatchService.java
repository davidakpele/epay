package com.example.admin_api_service.Interfaces;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.SettlementBatchStatus;
import com.example.admin_api_service.enums.SettlementBatchType;
import com.example.admin_api_service.models.settlementsAndReconciliation.SettlementBatch;

public interface ISettlementBatchService {
    SettlementBatch createBatch(SettlementBatchType batchType, LocalDate settlementDate,
                                String currency, String counterpartyName,
                                String counterpartyBankCode, String counterpartyAccountNumber,
                                Long systemWalletId, String initiatedBy, String notes);
 
    SettlementBatch getBatchById(String batchId);
 
    SettlementBatch getBatchByReference(String batchReference);
 
    Page<SettlementBatch> getAllBatches(Pageable pageable);
 
    Page<SettlementBatch> getBatchesByStatus(SettlementBatchStatus status, Pageable pageable);
 
    Page<SettlementBatch> getBatchesByType(SettlementBatchType type, Pageable pageable);
 
    Page<SettlementBatch> getBatchesBySettlementDate(LocalDate settlementDate, Pageable pageable);
 
    SettlementBatch approveBatch(String batchId, String approvedBy);
 
    SettlementBatch startProcessing(String batchId);
 
    // Incremental counter update called as each record is settled
    SettlementBatch updateTotals(String batchId, boolean success,
                                 BigDecimal grossAmount, BigDecimal feeAmount, BigDecimal netAmount);
 
    SettlementBatch completeBatch(String batchId, String externalSettlementReference);
 
    SettlementBatch failBatch(String batchId, String failureReason);
 
    SettlementBatch cancelBatch(String batchId, String cancelledBy, String reason);
 
    SettlementBatch reverseBatch(String batchId, String reversedBy, String reason);
}
