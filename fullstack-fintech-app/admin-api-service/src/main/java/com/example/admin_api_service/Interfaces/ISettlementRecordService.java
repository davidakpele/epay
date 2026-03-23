package com.example.admin_api_service.Interfaces;

import java.util.List;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.SettlementRecordStatus;
import com.example.admin_api_service.enums.SettlementRecordType;
import com.example.admin_api_service.models.settlementsAndReconciliation.SettlementRecord;

public interface ISettlementRecordService {
    SettlementRecord createRecord(String settlementBatchId, String transactionId,
                                  Long walletId, Long userId,
                                  SettlementRecordType recordType, String currency,
                                  BigDecimal grossAmount, BigDecimal feeAmount, BigDecimal netAmount,
                                  String counterpartyBankCode, String counterpartyAccountNumber,
                                  String counterpartyAccountName, String historyReferenceId);
 
    SettlementRecord getRecordById(String recordId);
 
    Page<SettlementRecord> getRecordsByBatch(String batchId, Pageable pageable);
 
    Page<SettlementRecord> getRecordsByBatchAndStatus(String batchId,
                                                      SettlementRecordStatus status,
                                                      Pageable pageable);
 
    Page<SettlementRecord> getRecordsByWallet(Long walletId, Pageable pageable);
 
    List<SettlementRecord> getFailedRecordsForBatch(String batchId);
 
    SettlementRecord markSettled(String recordId, String externalReference);
 
    SettlementRecord markFailed(String recordId, String failureReason);
 
    SettlementRecord retryRecord(String recordId);
 
    SettlementRecord reverseRecord(String recordId);
 
    void createRecordsBatch(String settlementBatchId, List<SettlementRecord> records);
}
