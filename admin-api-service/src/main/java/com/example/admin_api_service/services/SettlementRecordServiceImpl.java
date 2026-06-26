package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.ISettlementBatchService;
import com.example.admin_api_service.Interfaces.ISettlementRecordService;
import com.example.admin_api_service.enums.SettlementRecordStatus;
import com.example.admin_api_service.enums.SettlementRecordType;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.settlementsAndReconciliation.SettlementRecord;
import com.example.admin_api_service.repository.SettlementRecordRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SettlementRecordServiceImpl implements ISettlementRecordService {

    private final SettlementRecordRepository recordRepository;
    private final ISettlementBatchService batchService;

    public SettlementRecordServiceImpl(SettlementRecordRepository recordRepository,
                                        ISettlementBatchService batchService) {
        this.recordRepository = recordRepository;
        this.batchService = batchService;
    }

    @Override
    public SettlementRecord createRecord(String settlementBatchId, String transactionId,
                                         Long walletId, Long userId,
                                         SettlementRecordType recordType, String currency,
                                         BigDecimal grossAmount, BigDecimal feeAmount,
                                         BigDecimal netAmount, String counterpartyBankCode,
                                         String counterpartyAccountNumber,
                                         String counterpartyAccountName,
                                         String historyReferenceId) {
        batchService.getBatchById(settlementBatchId); // validate batch exists

        SettlementRecord record = new SettlementRecord();
        record.setSettlementBatchId(settlementBatchId);
        record.setTransactionId(transactionId);
        record.setWalletId(walletId);
        record.setUserId(userId);
        record.setRecordType(recordType);
        record.setCurrency(currency);
        record.setGrossAmount(grossAmount != null ? grossAmount : BigDecimal.ZERO);
        record.setFeeAmount(feeAmount != null ? feeAmount : BigDecimal.ZERO);
        record.setNetAmount(netAmount != null ? netAmount : BigDecimal.ZERO);
        record.setCounterpartyBankCode(counterpartyBankCode);
        record.setCounterpartyAccountNumber(counterpartyAccountNumber);
        record.setCounterpartyAccountName(counterpartyAccountName);
        record.setHistoryReferenceId(historyReferenceId);
        record.setStatus(SettlementRecordStatus.PENDING);
        return recordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementRecord getRecordById(String recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("SettlementRecord", "id", recordId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementRecord> getRecordsByBatch(String batchId, Pageable pageable) {
        return recordRepository.findAllBySettlementBatchId(batchId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementRecord> getRecordsByBatchAndStatus(String batchId,
                                                              SettlementRecordStatus status,
                                                              Pageable pageable) {
        return recordRepository.findAllBySettlementBatchIdAndStatus(batchId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementRecord> getRecordsByWallet(Long walletId, Pageable pageable) {
        return recordRepository.findAllByWalletId(walletId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementRecord> getFailedRecordsForBatch(String batchId) {
        return recordRepository.findAllBySettlementBatchIdAndStatus(
                batchId, SettlementRecordStatus.FAILED);
    }

    @Override
    public SettlementRecord markSettled(String recordId, String externalReference) {
        SettlementRecord record = getRecordById(recordId);
        record.setStatus(SettlementRecordStatus.SETTLED);
        record.setExternalReference(externalReference);
        record.setSettledAt(LocalDateTime.now());
        record.setUpdatedOn(LocalDateTime.now());

        SettlementRecord saved = recordRepository.save(record);
        batchService.updateTotals(record.getSettlementBatchId(), true,
                record.getGrossAmount(), record.getFeeAmount(), record.getNetAmount());
        return saved;
    }

    @Override
    public SettlementRecord markFailed(String recordId, String failureReason) {
        SettlementRecord record = getRecordById(recordId);
        record.setStatus(SettlementRecordStatus.FAILED);
        record.setFailureReason(failureReason);
        record.setUpdatedOn(LocalDateTime.now());

        SettlementRecord saved = recordRepository.save(record);
        batchService.updateTotals(record.getSettlementBatchId(), false,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        return saved;
    }

    @Override
    public SettlementRecord retryRecord(String recordId) {
        SettlementRecord record = getRecordById(recordId);
        if (record.getStatus() != SettlementRecordStatus.FAILED) {
            throw new BadRequestException("Only failed records can be retried");
        }
        record.setStatus(SettlementRecordStatus.PENDING);
        record.setRetryCount(record.getRetryCount() + 1);
        record.setFailureReason(null);
        record.setUpdatedOn(LocalDateTime.now());
        return recordRepository.save(record);
    }

    @Override
    public SettlementRecord reverseRecord(String recordId) {
        SettlementRecord record = getRecordById(recordId);
        if (record.getStatus() != SettlementRecordStatus.SETTLED) {
            throw new ConflictException("Only settled records can be reversed");
        }
        record.setStatus(SettlementRecordStatus.REVERSED);
        record.setUpdatedOn(LocalDateTime.now());
        return recordRepository.save(record);
    }

    @Override
    public void createRecordsBatch(String settlementBatchId, List<SettlementRecord> records) {
        batchService.getBatchById(settlementBatchId);
        records.forEach(r -> {
            r.setSettlementBatchId(settlementBatchId);
            r.setStatus(SettlementRecordStatus.PENDING);
            if (r.getGrossAmount() == null) r.setGrossAmount(BigDecimal.ZERO);
            if (r.getFeeAmount() == null) r.setFeeAmount(BigDecimal.ZERO);
            if (r.getNetAmount() == null) r.setNetAmount(BigDecimal.ZERO);
        });
        recordRepository.saveAll(records);
    }
}
