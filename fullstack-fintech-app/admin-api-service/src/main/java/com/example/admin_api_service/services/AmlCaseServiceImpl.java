package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAmlCaseService;
import com.example.admin_api_service.enums.AmlCasePriority;
import com.example.admin_api_service.enums.AmlCaseStatus;
import com.example.admin_api_service.enums.AmlCaseType;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.complianceAndRisk.AmlCase;
import com.example.admin_api_service.repository.AmlCaseRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class AmlCaseServiceImpl implements IAmlCaseService {

    private final AmlCaseRepository amlCaseRepository;
    private static final AtomicLong caseCounter = new AtomicLong();

    public AmlCaseServiceImpl(AmlCaseRepository amlCaseRepository) {
        this.amlCaseRepository = amlCaseRepository;
    }

    @Override
    public AmlCase openCase(Long userId, Long walletId, AmlCaseType caseType,
                            AmlCasePriority priority, String title, String description,
                            String openedBy) {
        AmlCase amlCase = new AmlCase();
        amlCase.setCaseReference(generateCaseReference());
        amlCase.setUserId(userId);
        amlCase.setWalletId(walletId);
        amlCase.setCaseType(caseType);
        amlCase.setPriority(priority);
        amlCase.setTitle(title);
        amlCase.setDescription(description);
        amlCase.setOpenedBy(openedBy);
        amlCase.setOpenedAt(LocalDateTime.now());
        amlCase.setStatus(AmlCaseStatus.OPEN);
        return amlCaseRepository.save(amlCase);
    }

    @Override
    @Transactional(readOnly = true)
    public AmlCase getCaseById(String caseId) {
        return amlCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("AmlCase", "id", caseId));
    }

    @Override
    @Transactional(readOnly = true)
    public AmlCase getCaseByCaseReference(String caseReference) {
        return amlCaseRepository.findByCaseReference(caseReference)
                .orElseThrow(() -> new ResourceNotFoundException("AmlCase", "caseReference", caseReference));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AmlCase> getAllCases(Pageable pageable) {
        return amlCaseRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AmlCase> getCasesByStatus(AmlCaseStatus status, Pageable pageable) {
        return amlCaseRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AmlCase> getCasesByPriority(AmlCasePriority priority, Pageable pageable) {
        return amlCaseRepository.findAllByPriority(priority, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AmlCase> getCasesByUser(Long userId, Pageable pageable) {
        return amlCaseRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public AmlCase assignCase(String caseId, String assignedTo) {
        AmlCase amlCase = getCaseById(caseId);
        validateCaseIsOpen(amlCase);
        amlCase.setAssignedTo(assignedTo);
        amlCase.setAssignedAt(LocalDateTime.now());
        amlCase.setStatus(AmlCaseStatus.UNDER_INVESTIGATION);
        amlCase.setUpdatedOn(LocalDateTime.now());
        return amlCaseRepository.save(amlCase);
    }

    @Override
    public AmlCase escalateCase(String caseId, String escalatedTo, String escalationReason) {
        AmlCase amlCase = getCaseById(caseId);
        validateCaseIsOpen(amlCase);
        amlCase.setStatus(AmlCaseStatus.ESCALATED);
        amlCase.setEscalatedTo(escalatedTo);
        amlCase.setEscalatedAt(LocalDateTime.now());
        amlCase.setEscalationReason(escalationReason);
        amlCase.setUpdatedOn(LocalDateTime.now());
        return amlCaseRepository.save(amlCase);
    }

    @Override
    public AmlCase fileSar(String caseId, String sarReference, String sarFiledBy) {
        AmlCase amlCase = getCaseById(caseId);
        if (amlCase.isSarFiled()) {
            throw new ConflictException("SAR has already been filed for case: " + caseId);
        }
        amlCase.setSarFiled(true);
        amlCase.setSarReference(sarReference);
        amlCase.setSarFiledBy(sarFiledBy);
        amlCase.setSarFiledAt(LocalDateTime.now());
        amlCase.setStatus(AmlCaseStatus.SAR_FILED);
        amlCase.setUpdatedOn(LocalDateTime.now());
        return amlCaseRepository.save(amlCase);
    }

    @Override
    public AmlCase resolveCase(String caseId, boolean suspiciousActivityConfirmed,
                               String closedBy, String closureNote) {
        AmlCase amlCase = getCaseById(caseId);
        validateCaseIsOpen(amlCase);
        amlCase.setStatus(suspiciousActivityConfirmed
                ? AmlCaseStatus.RESOLVED_CONFIRMED
                : AmlCaseStatus.RESOLVED_CLEARED);
        amlCase.setClosedBy(closedBy);
        amlCase.setClosedAt(LocalDateTime.now());
        amlCase.setClosureNote(closureNote);
        amlCase.setUpdatedOn(LocalDateTime.now());
        return amlCaseRepository.save(amlCase);
    }

    @Override
    public AmlCase closeCase(String caseId, String closedBy, String closureNote) {
        AmlCase amlCase = getCaseById(caseId);
        amlCase.setStatus(AmlCaseStatus.CLOSED);
        amlCase.setClosedBy(closedBy);
        amlCase.setClosedAt(LocalDateTime.now());
        amlCase.setClosureNote(closureNote);
        amlCase.setUpdatedOn(LocalDateTime.now());
        return amlCaseRepository.save(amlCase);
    }

    @Override
    public AmlCase linkFreezeToCase(String caseId, String freezeId) {
        AmlCase amlCase = getCaseById(caseId);
        amlCase.setTriggeredFreezeId(freezeId);
        amlCase.setUpdatedOn(LocalDateTime.now());
        return amlCaseRepository.save(amlCase);
    }

    @Override
    public AmlCase updateActivitySummary(String caseId, String activitySummaryJson) {
        AmlCase amlCase = getCaseById(caseId);
        amlCase.setActivitySummary(activitySummaryJson);
        amlCase.setUpdatedOn(LocalDateTime.now());
        return amlCaseRepository.save(amlCase);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private String generateCaseReference() {
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long seq = amlCaseRepository.count() + caseCounter.incrementAndGet();
        return String.format("AML-%s-%05d", year, seq);
    }

    private void validateCaseIsOpen(AmlCase amlCase) {
        if (amlCase.getStatus() == AmlCaseStatus.CLOSED
                || amlCase.getStatus() == AmlCaseStatus.RESOLVED_CLEARED
                || amlCase.getStatus() == AmlCaseStatus.RESOLVED_CONFIRMED) {
            throw new ConflictException("Case is already in a terminal state: " + amlCase.getStatus());
        }
    }
}
