package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAmlCaseService;
import com.example.admin_api_service.Interfaces.ISanctionListService;
import com.example.admin_api_service.Interfaces.ISanctionScreeningLogService;
import com.example.admin_api_service.enums.AmlCasePriority;
import com.example.admin_api_service.enums.AmlCaseType;
import com.example.admin_api_service.enums.SanctionScreeningResult;
import com.example.admin_api_service.enums.SanctionScreeningTrigger;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.complianceAndRisk.AmlCase;
import com.example.admin_api_service.models.complianceAndRisk.SanctionList;
import com.example.admin_api_service.models.complianceAndRisk.SanctionScreeningLog;
import com.example.admin_api_service.repository.SanctionScreeningLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SanctionScreeningLogServiceImpl implements ISanctionScreeningLogService {

    private static final double POTENTIAL_MATCH_THRESHOLD = 0.70;
    private static final double CONFIRMED_MATCH_THRESHOLD = 0.90;

    private final SanctionScreeningLogRepository screeningLogRepository;
    private final ISanctionListService sanctionListService;
    private final IAmlCaseService amlCaseService;

    public SanctionScreeningLogServiceImpl(SanctionScreeningLogRepository screeningLogRepository,
                                            ISanctionListService sanctionListService,
                                            IAmlCaseService amlCaseService) {
        this.screeningLogRepository = screeningLogRepository;
        this.sanctionListService = sanctionListService;
        this.amlCaseService = amlCaseService;
    }

    @Override
    public SanctionScreeningLog screen(Long userId, Long walletId, String transactionId,
                                       SanctionScreeningTrigger trigger, String screenedName) {
        SanctionScreeningLog log = new SanctionScreeningLog();
        log.setUserId(userId);
        log.setWalletId(walletId);
        log.setTransactionId(transactionId);
        log.setTrigger(trigger);
        log.setScreenedName(screenedName);
        log.setScreenedAt(LocalDateTime.now());
        log.setProviderName("INTERNAL");

        // Run name search against sanction list
        List<SanctionList> candidates = sanctionListService.searchByName(
                screenedName, POTENTIAL_MATCH_THRESHOLD);

        if (candidates.isEmpty()) {
            log.setResult(SanctionScreeningResult.CLEAR);
            log.setMatchScore(BigDecimal.ZERO);
        } else {
            SanctionList topMatch = candidates.get(0);
            double score = calculateScore(screenedName, topMatch.getName());
            log.setMatchScore(BigDecimal.valueOf(score * 100));
            log.setMatchedSanctionId(topMatch.getId());

            if (score >= CONFIRMED_MATCH_THRESHOLD) {
                log.setResult(SanctionScreeningResult.CONFIRMED_MATCH);
            } else {
                log.setResult(SanctionScreeningResult.POTENTIAL_MATCH);
            }
        }

        return screeningLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public SanctionScreeningLog getLogById(String logId) {
        return screeningLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("SanctionScreeningLog", "id", logId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SanctionScreeningLog> getAllLogs(Pageable pageable) {
        return screeningLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SanctionScreeningLog> getLogsByUser(Long userId, Pageable pageable) {
        return screeningLogRepository.findAllByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SanctionScreeningLog> getLogsByResult(SanctionScreeningResult result, Pageable pageable) {
        return screeningLogRepository.findAllByResult(result, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SanctionScreeningLog> getLogsByTransaction(String transactionId, Pageable pageable) {
        return screeningLogRepository.findAllByTransactionId(transactionId, pageable);
    }

    @Override
    public SanctionScreeningLog reviewLog(String logId, String reviewedBy, String reviewNote,
                                          SanctionScreeningResult overrideResult) {
        SanctionScreeningLog log = getLogById(logId);
        log.setManuallyReviewed(true);
        log.setReviewedBy(reviewedBy);
        log.setReviewedAt(LocalDateTime.now());
        log.setReviewNote(reviewNote);
        if (overrideResult != null) {
            log.setResult(overrideResult);
        }
        return screeningLogRepository.save(log);
    }

    @Override
    public SanctionScreeningLog escalateToAmlCase(String logId, String escalatedBy) {
        SanctionScreeningLog log = getLogById(logId);

        if (log.getResult() != SanctionScreeningResult.POTENTIAL_MATCH
                && log.getResult() != SanctionScreeningResult.CONFIRMED_MATCH) {
            throw new BadRequestException("Only POTENTIAL_MATCH or CONFIRMED_MATCH results can be escalated");
        }
        if (log.getAmlCaseId() != null) {
            throw new BadRequestException("This screening log is already linked to an AML case");
        }

        AmlCase amlCase = amlCaseService.openCase(
                log.getUserId(),
                log.getWalletId(),
                AmlCaseType.SANCTION_MATCH,
                AmlCasePriority.CRITICAL,
                "Sanction match: " + log.getScreenedName(),
                "Screening result: " + log.getResult() + " | Match score: " + log.getMatchScore(),
                escalatedBy
        );

        log.setAmlCaseId(amlCase.getId());
        return screeningLogRepository.save(log);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private double calculateScore(String query, String candidate) {
        if (query == null || candidate == null) return 0.0;
        String[] qt = query.toLowerCase().split("\\s+");
        String[] ct = candidate.toLowerCase().split("\\s+");
        long matches = 0;
        for (String q : qt) {
            for (String c : ct) {
                if (c.contains(q) || q.contains(c)) { matches++; break; }
            }
        }
        return (double) matches / Math.max(qt.length, ct.length);
    }
}