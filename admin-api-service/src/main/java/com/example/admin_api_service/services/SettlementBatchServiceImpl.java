package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.ISettlementBatchService;
import com.example.admin_api_service.enums.SettlementBatchStatus;
import com.example.admin_api_service.enums.SettlementBatchType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.settlementsAndReconciliation.SettlementBatch;
import com.example.admin_api_service.repository.SettlementBatchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class SettlementBatchServiceImpl implements ISettlementBatchService {

    private final SettlementBatchRepository batchRepository;
    private static final AtomicLong batchCounter = new AtomicLong();

    public SettlementBatchServiceImpl(SettlementBatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    @Override
    public SettlementBatch createBatch(SettlementBatchType batchType, LocalDate settlementDate,
                                       String currency, String counterpartyName,
                                       String counterpartyBankCode, String counterpartyAccountNumber,
                                       Long systemWalletId, String initiatedBy, String notes) {
        SettlementBatch batch = new SettlementBatch();
        batch.setBatchReference(generateBatchReference(settlementDate));
        batch.setBatchType(batchType);
        batch.setSettlementDate(settlementDate);
        batch.setPeriodStart(settlementDate.atStartOfDay());
        batch.setPeriodEnd(settlementDate.plusDays(1).atStartOfDay().minusNanos(1));
        batch.setCurrency(currency);
        batch.setCounterpartyName(counterpartyName);
        batch.setCounterpartyBankCode(counterpartyBankCode);
        batch.setCounterpartyAccountNumber(counterpartyAccountNumber);
        batch.setSystemWalletId(systemWalletId);
        batch.setInitiatedBy(initiatedBy);
        batch.setNotes(notes);
        batch.setStatus(SettlementBatchStatus.PENDING);
        batch.setTotalCreditAmount(BigDecimal.ZERO);
        batch.setTotalDebitAmount(BigDecimal.ZERO);
        batch.setTotalFeeAmount(BigDecimal.ZERO);
        batch.setNetSettlementAmount(BigDecimal.ZERO);
        return batchRepository.save(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementBatch getBatchById(String batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("SettlementBatch", "id", batchId));
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementBatch getBatchByReference(String batchReference) {
        return batchRepository.findByBatchReference(batchReference)
            .orElseThrow(() -> new ResourceNotFoundException("SettlementBatch", "batchReference", batchReference));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementBatch> getAllBatches(Pageable pageable) {
        return batchRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementBatch> getBatchesByStatus(SettlementBatchStatus status, Pageable pageable) {
        return batchRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementBatch> getBatchesByType(SettlementBatchType type, Pageable pageable) {
        return batchRepository.findAllByBatchType(type, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SettlementBatch> getBatchesBySettlementDate(LocalDate settlementDate, Pageable pageable) {
        return batchRepository.findAllBySettlementDate(settlementDate, pageable);
    }

    @Override
    public SettlementBatch approveBatch(String batchId, String approvedBy) {
        SettlementBatch batch = getBatchById(batchId);
        if (batch.getStatus() != SettlementBatchStatus.PENDING
                && batch.getStatus() != SettlementBatchStatus.AWAITING_APPROVAL) {
            throw new ConflictException("Batch cannot be approved in its current state: " + batch.getStatus());
        }
        batch.setStatus(SettlementBatchStatus.APPROVED);
        batch.setApprovedBy(approvedBy);
        batch.setApprovedAt(LocalDateTime.now());
        batch.setUpdatedOn(LocalDateTime.now());
        return batchRepository.save(batch);
    }

    @Override
    public SettlementBatch startProcessing(String batchId) {
        SettlementBatch batch = getBatchById(batchId);
        if (batch.getStatus() != SettlementBatchStatus.APPROVED) {
            throw new ConflictException("Batch must be approved before processing. Status: " + batch.getStatus());
        }
        batch.setStatus(SettlementBatchStatus.PROCESSING);
        batch.setProcessedAt(LocalDateTime.now());
        batch.setUpdatedOn(LocalDateTime.now());
        return batchRepository.save(batch);
    }

    @Override
    public SettlementBatch updateTotals(String batchId, boolean success,
                                        BigDecimal grossAmount, BigDecimal feeAmount,
                                        BigDecimal netAmount) {
        SettlementBatch batch = getBatchById(batchId);
        batch.setTotalRecords(batch.getTotalRecords() + 1);
        if (success) {
            batch.setSuccessfulRecords(batch.getSuccessfulRecords() + 1);
            batch.setTotalCreditAmount(batch.getTotalCreditAmount().add(grossAmount != null ? grossAmount : BigDecimal.ZERO));
            batch.setTotalFeeAmount(batch.getTotalFeeAmount().add(feeAmount != null ? feeAmount : BigDecimal.ZERO));
            batch.setNetSettlementAmount(batch.getNetSettlementAmount().add(netAmount != null ? netAmount : BigDecimal.ZERO));
        } else {
            batch.setFailedRecords(batch.getFailedRecords() + 1);
        }
        batch.setUpdatedOn(LocalDateTime.now());
        return batchRepository.save(batch);
    }

    @Override
    public SettlementBatch completeBatch(String batchId, String externalSettlementReference) {
        SettlementBatch batch = getBatchById(batchId);
        boolean hasFailures = batch.getFailedRecords() > 0;
        batch.setStatus(hasFailures
                ? SettlementBatchStatus.PARTIALLY_COMPLETED
                : SettlementBatchStatus.COMPLETED);
        batch.setExternalSettlementReference(externalSettlementReference);
        batch.setCompletedAt(LocalDateTime.now());
        batch.setUpdatedOn(LocalDateTime.now());
        return batchRepository.save(batch);
    }

    @Override
    public SettlementBatch failBatch(String batchId, String failureReason) {
        SettlementBatch batch = getBatchById(batchId);
        batch.setStatus(SettlementBatchStatus.FAILED);
        batch.setFailureReason(failureReason);
        batch.setUpdatedOn(LocalDateTime.now());
        return batchRepository.save(batch);
    }

    @Override
    public SettlementBatch cancelBatch(String batchId, String cancelledBy, String reason) {
        SettlementBatch batch = getBatchById(batchId);
        if (batch.getStatus() == SettlementBatchStatus.COMPLETED
                || batch.getStatus() == SettlementBatchStatus.REVERSED) {
            throw new ConflictException("Cannot cancel a batch that is already " + batch.getStatus());
        }
        batch.setStatus(SettlementBatchStatus.CANCELLED);
        batch.setNotes(reason);
        batch.setUpdatedOn(LocalDateTime.now());
        return batchRepository.save(batch);
    }

    @Override
    public SettlementBatch reverseBatch(String batchId, String reversedBy, String reason) {
        SettlementBatch batch = getBatchById(batchId);
        if (batch.getStatus() != SettlementBatchStatus.COMPLETED
                && batch.getStatus() != SettlementBatchStatus.PARTIALLY_COMPLETED) {
            throw new ConflictException("Only completed batches can be reversed. Status: " + batch.getStatus());
        }
        batch.setStatus(SettlementBatchStatus.REVERSED);
        batch.setNotes(reason);
        batch.setUpdatedOn(LocalDateTime.now());
        return batchRepository.save(batch);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private String generateBatchReference(LocalDate settlementDate) {
        String date = settlementDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = batchRepository.countBySettlementDate(settlementDate) + batchCounter.incrementAndGet();
        return String.format("STL-%s-%03d", date, seq);
    }
}
