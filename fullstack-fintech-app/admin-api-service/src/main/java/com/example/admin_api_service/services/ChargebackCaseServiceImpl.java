package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IChargebackCaseService;
import com.example.admin_api_service.enums.ChargebackCaseStatus;
import com.example.admin_api_service.enums.ChargebackOutcome;
import com.example.admin_api_service.enums.ChargebackReason;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.settlementsAndReconciliation.ChargebackCase;
import com.example.admin_api_service.repository.ChargebackCaseRepository;

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
public class ChargebackCaseServiceImpl implements IChargebackCaseService {

    private final ChargebackCaseRepository chargebackRepository;
    private static final AtomicLong caseCounter = new AtomicLong();

    public ChargebackCaseServiceImpl(ChargebackCaseRepository chargebackRepository) {
        this.chargebackRepository = chargebackRepository;
    }

    @Override
    public ChargebackCase raiseCase(String transactionId, Long walletId, Long userId,
                                    ChargebackReason reason, String currency,
                                    BigDecimal disputedAmount, String description,
                                    String raisedBy) {
        ChargebackCase chargebackCase = new ChargebackCase();
        chargebackCase.setCaseReference(generateCaseReference());
        chargebackCase.setTransactionId(transactionId);
        chargebackCase.setWalletId(walletId);
        chargebackCase.setUserId(userId);
        chargebackCase.setReason(reason);
        chargebackCase.setCurrency(currency);
        chargebackCase.setDisputedAmount(disputedAmount);
        chargebackCase.setChargebackFee(BigDecimal.ZERO);
        chargebackCase.setDescription(description);
        chargebackCase.setRaisedBy(raisedBy);
        chargebackCase.setRaisedAt(LocalDateTime.now());
        chargebackCase.setStatus(ChargebackCaseStatus.RAISED);
        return chargebackRepository.save(chargebackCase);
    }

    @Override
    @Transactional(readOnly = true)
    public ChargebackCase getCaseById(String caseId) {
        return chargebackRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("ChargebackCase", "id", caseId));
    }

    @Override
    @Transactional(readOnly = true)
    public ChargebackCase getCaseByCaseReference(String caseReference) {
        return chargebackRepository.findByCaseReference(caseReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ChargebackCase", "caseReference", caseReference));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChargebackCase> getAllCases(Pageable pageable) {
        return chargebackRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChargebackCase> getCasesByStatus(ChargebackCaseStatus status, Pageable pageable) {
        return chargebackRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChargebackCase> getCasesByUser(Long userId, Pageable pageable) {
        return chargebackRepository.findAllByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChargebackCase> getCasesByWallet(Long walletId, Pageable pageable) {
        return chargebackRepository.findAllByWalletId(walletId, pageable);
    }

    @Override
    public ChargebackCase assignCase(String caseId, String assignedTo) {
        ChargebackCase chargebackCase = getCaseById(caseId);
        validateIsOpen(chargebackCase);
        chargebackCase.setAssignedTo(assignedTo);
        chargebackCase.setAssignedAt(LocalDateTime.now());
        chargebackCase.setStatus(ChargebackCaseStatus.UNDER_REVIEW);
        chargebackCase.setUpdatedOn(LocalDateTime.now());
        return chargebackRepository.save(chargebackCase);
    }

    @Override
    public ChargebackCase submitEvidence(String caseId, String evidenceUrlsJson, String submittedBy) {
        ChargebackCase chargebackCase = getCaseById(caseId);
        validateIsOpen(chargebackCase);
        chargebackCase.setEvidenceUrls(evidenceUrlsJson);
        chargebackCase.setStatus(ChargebackCaseStatus.EVIDENCE_SUBMITTED);
        chargebackCase.setUpdatedOn(LocalDateTime.now());
        return chargebackRepository.save(chargebackCase);
    }

    @Override
    public ChargebackCase filePreArbitration(String caseId, LocalDate preArbitrationDate,
                                              String filedBy) {
        ChargebackCase chargebackCase = getCaseById(caseId);
        validateIsOpen(chargebackCase);
        chargebackCase.setPreArbitrationFiled(true);
        chargebackCase.setPreArbitrationDate(preArbitrationDate);
        chargebackCase.setStatus(ChargebackCaseStatus.PRE_ARBITRATION);
        chargebackCase.setUpdatedOn(LocalDateTime.now());
        return chargebackRepository.save(chargebackCase);
    }

    @Override
    public ChargebackCase fileArbitration(String caseId, LocalDate arbitrationDate,
                                           String filedBy) {
        ChargebackCase chargebackCase = getCaseById(caseId);
        validateIsOpen(chargebackCase);
        chargebackCase.setArbitrationFiled(true);
        chargebackCase.setArbitrationDate(arbitrationDate);
        chargebackCase.setStatus(ChargebackCaseStatus.ARBITRATION);
        chargebackCase.setUpdatedOn(LocalDateTime.now());
        return chargebackRepository.save(chargebackCase);
    }

    @Override
    public ChargebackCase resolveCase(String caseId, ChargebackOutcome outcome,
                                      BigDecimal recoveredAmount, String resolvedBy,
                                      String resolutionNote) {
        ChargebackCase chargebackCase = getCaseById(caseId);
        validateIsOpen(chargebackCase);

        chargebackCase.setOutcome(outcome);
        chargebackCase.setRecoveredAmount(recoveredAmount);
        chargebackCase.setResolvedBy(resolvedBy);
        chargebackCase.setResolvedAt(LocalDateTime.now());
        chargebackCase.setResolutionNote(resolutionNote);

        chargebackCase.setStatus(switch (outcome) {
            case WON -> ChargebackCaseStatus.WON;
            case LOST -> ChargebackCaseStatus.LOST;
            case PARTIAL_WIN -> ChargebackCaseStatus.WON;
            case ACCEPTED -> ChargebackCaseStatus.REVERSED;
            case WRITTEN_OFF -> ChargebackCaseStatus.CLOSED;
        });

        chargebackCase.setUpdatedOn(LocalDateTime.now());
        return chargebackRepository.save(chargebackCase);
    }

    @Override
    public ChargebackCase cancelCase(String caseId, String cancelledBy, String reason) {
        ChargebackCase chargebackCase = getCaseById(caseId);
        validateIsOpen(chargebackCase);
        chargebackCase.setStatus(ChargebackCaseStatus.CANCELLED);
        chargebackCase.setResolutionNote(reason);
        chargebackCase.setResolvedBy(cancelledBy);
        chargebackCase.setResolvedAt(LocalDateTime.now());
        chargebackCase.setUpdatedOn(LocalDateTime.now());
        return chargebackRepository.save(chargebackCase);
    }

    @Override
    public ChargebackCase linkWalletFreeze(String caseId, String freezeId) {
        ChargebackCase chargebackCase = getCaseById(caseId);
        chargebackCase.setTriggeredFreezeId(freezeId);
        chargebackCase.setUpdatedOn(LocalDateTime.now());
        return chargebackRepository.save(chargebackCase);
    }

    @Override
    public ChargebackCase linkReversalAdjustment(String caseId, String adjustmentId) {
        ChargebackCase chargebackCase = getCaseById(caseId);
        chargebackCase.setReversalAdjustmentId(adjustmentId);
        chargebackCase.setUpdatedOn(LocalDateTime.now());
        return chargebackRepository.save(chargebackCase);
    }

    @Override
    public ChargebackCase updateNetworkReference(String caseId, String networkReference,
                                                  String acquirerReference) {
        ChargebackCase chargebackCase = getCaseById(caseId);
        chargebackCase.setNetworkReference(networkReference);
        chargebackCase.setAcquirerReference(acquirerReference);
        chargebackCase.setUpdatedOn(LocalDateTime.now());
        return chargebackRepository.save(chargebackCase);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private String generateCaseReference() {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long seq = chargebackRepository.count() + caseCounter.incrementAndGet();
        return String.format("CHB-%s-%05d", year, seq);
    }

    private void validateIsOpen(ChargebackCase chargebackCase) {
        if (chargebackCase.getStatus() == ChargebackCaseStatus.WON
                || chargebackCase.getStatus() == ChargebackCaseStatus.LOST
                || chargebackCase.getStatus() == ChargebackCaseStatus.REVERSED
                || chargebackCase.getStatus() == ChargebackCaseStatus.CANCELLED
                || chargebackCase.getStatus() == ChargebackCaseStatus.CLOSED) {
            throw new ConflictException("Case is already in a terminal state: "
                    + chargebackCase.getStatus());
        }
    }
}