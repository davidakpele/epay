package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IChargebackCaseService;
import com.example.admin_api_service.Interfaces.IReconciliationExceptionService;
import com.example.admin_api_service.enums.ChargebackReason;
import com.example.admin_api_service.enums.ReconciliationExceptionStatus;
import com.example.admin_api_service.enums.ReconciliationExceptionType;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.settlementsAndReconciliation.ChargebackCase;
import com.example.admin_api_service.models.settlementsAndReconciliation.ReconciliationException;
import com.example.admin_api_service.repository.ReconciliationExceptionRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReconciliationExceptionServiceImpl implements IReconciliationExceptionService {

    private final ReconciliationExceptionRepository exceptionRepository;
    private final IChargebackCaseService chargebackCaseService;

    public ReconciliationExceptionServiceImpl(ReconciliationExceptionRepository exceptionRepository,
                                               @Lazy IChargebackCaseService chargebackCaseService) {
        this.exceptionRepository = exceptionRepository;
        this.chargebackCaseService = chargebackCaseService;
    }

    @Override
    public ReconciliationException createException(String reconciliationReportId,
                                                   ReconciliationExceptionType exceptionType,
                                                   String internalTransactionId,
                                                   String externalReference,
                                                   String currency,
                                                   BigDecimal internalAmount,
                                                   BigDecimal externalAmount,
                                                   BigDecimal varianceAmount,
                                                   String description) {
        ReconciliationException exception = new ReconciliationException();
        exception.setReconciliationReportId(reconciliationReportId);
        exception.setExceptionType(exceptionType);
        exception.setInternalTransactionId(internalTransactionId);
        exception.setExternalReference(externalReference);
        exception.setCurrency(currency);
        exception.setInternalAmount(internalAmount);
        exception.setExternalAmount(externalAmount);
        exception.setVarianceAmount(varianceAmount);
        exception.setDescription(description);
        exception.setStatus(ReconciliationExceptionStatus.OPEN);
        return exceptionRepository.save(exception);
    }

    @Override
    @Transactional(readOnly = true)
    public ReconciliationException getExceptionById(String exceptionId) {
        return exceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ReconciliationException", "id", exceptionId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReconciliationException> getAllExceptions(Pageable pageable) {
        return exceptionRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReconciliationException> getExceptionsByReport(String reportId, Pageable pageable) {
        return exceptionRepository.findAllByReconciliationReportId(reportId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReconciliationException> getExceptionsByStatus(ReconciliationExceptionStatus status,
                                                               Pageable pageable) {
        return exceptionRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReconciliationException> getOpenExceptionsForReport(String reportId) {
        return exceptionRepository.findAllByReconciliationReportIdAndStatusIn(
                reportId,
                List.of(ReconciliationExceptionStatus.OPEN,
                        ReconciliationExceptionStatus.UNDER_INVESTIGATION,
                        ReconciliationExceptionStatus.PENDING_ADJUSTMENT));
    }

    @Override
    public ReconciliationException assignException(String exceptionId, String assignedTo) {
        ReconciliationException exception = getExceptionById(exceptionId);
        validateIsActionable(exception);
        exception.setAssignedTo(assignedTo);
        exception.setStatus(ReconciliationExceptionStatus.UNDER_INVESTIGATION);
        exception.setUpdatedOn(LocalDateTime.now());
        return exceptionRepository.save(exception);
    }

    @Override
    public ReconciliationException resolveException(String exceptionId, String resolvedBy,
                                                    String resolutionNote) {
        ReconciliationException exception = getExceptionById(exceptionId);
        validateIsActionable(exception);
        exception.setStatus(ReconciliationExceptionStatus.RESOLVED);
        exception.setResolvedBy(resolvedBy);
        exception.setResolvedAt(LocalDateTime.now());
        exception.setResolutionNote(resolutionNote);
        exception.setUpdatedOn(LocalDateTime.now());
        return exceptionRepository.save(exception);
    }

    @Override
    public ReconciliationException writeOffException(String exceptionId, String resolvedBy,
                                                     String resolutionNote) {
        ReconciliationException exception = getExceptionById(exceptionId);
        validateIsActionable(exception);
        exception.setStatus(ReconciliationExceptionStatus.WRITTEN_OFF);
        exception.setResolvedBy(resolvedBy);
        exception.setResolvedAt(LocalDateTime.now());
        exception.setResolutionNote(resolutionNote);
        exception.setUpdatedOn(LocalDateTime.now());
        return exceptionRepository.save(exception);
    }

    @Override
    public ReconciliationException escalateToChargeback(String exceptionId, String escalatedBy) {
        ReconciliationException exception = getExceptionById(exceptionId);
        validateIsActionable(exception);

        if (exception.getInternalTransactionId() == null) {
            throw new BadRequestException(
                    "Cannot escalate to chargeback: no internal transaction ID on this exception");
        }
        if (exception.getChargebackCaseId() != null) {
            throw new ConflictException("Exception is already linked to chargeback case: "
                    + exception.getChargebackCaseId());
        }

        ChargebackCase chargebackCase = chargebackCaseService.raiseCase(
                exception.getInternalTransactionId(),
                null, null,
                ChargebackReason.TRANSACTION_NOT_RECEIVED,
                exception.getCurrency(),
                exception.getVarianceAmount() != null
                        ? exception.getVarianceAmount() : BigDecimal.ZERO,
                "Escalated from reconciliation exception: " + exceptionId,
                escalatedBy
        );

        exception.setChargebackCaseId(chargebackCase.getId());
        exception.setStatus(ReconciliationExceptionStatus.ESCALATED_TO_CHARGEBACK);
        exception.setUpdatedOn(LocalDateTime.now());
        return exceptionRepository.save(exception);
    }

    @Override
    public ReconciliationException linkManualAdjustment(String exceptionId, String manualAdjustmentId) {
        ReconciliationException exception = getExceptionById(exceptionId);
        exception.setManualAdjustmentId(manualAdjustmentId);
        exception.setStatus(ReconciliationExceptionStatus.PENDING_ADJUSTMENT);
        exception.setUpdatedOn(LocalDateTime.now());
        return exceptionRepository.save(exception);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void validateIsActionable(ReconciliationException exception) {
        if (exception.getStatus() == ReconciliationExceptionStatus.RESOLVED
                || exception.getStatus() == ReconciliationExceptionStatus.WRITTEN_OFF
                || exception.getStatus() == ReconciliationExceptionStatus.ESCALATED_TO_CHARGEBACK
                || exception.getStatus() == ReconciliationExceptionStatus.CLOSED) {
            throw new ConflictException("Exception is already in a terminal state: "
                    + exception.getStatus());
        }
    }
}
