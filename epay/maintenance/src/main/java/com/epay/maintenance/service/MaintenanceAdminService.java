package com.epay.maintenance.service;

import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.interfaces.UserLookupPort;
import com.epay.domain.maintenance.dto.*;
import com.epay.domain.maintenance.entity.*;
import com.epay.domain.maintenance.enums.DebtStatus;
import com.epay.domain.maintenance.enums.MaintenanceFeeStatus;
import com.epay.maintenance.repository.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceAdminService {

    private final MaintenanceFeeTransactionRepository feeTransactionRepo;
    private final UserDebtRepository                 debtRepo;
    private final UserMonthlyActivityRepository      activityRepo;
    private final MaintenanceFeeAuditLogRepository   auditLogRepo;
    private final MaintenanceFeeConfigService        configService;
    private final UserLookupPort                     userLookupPort;


    public DashboardOverviewDto getDashboardOverview(LocalDate billingMonth) {
        LocalDate month = billingMonth != null
                ? billingMonth.withDayOfMonth(1)
                : LocalDate.now().minusMonths(1).withDayOfMonth(1);

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (Object[] row : feeTransactionRepo.countByStatusForMonth(month)) {
            statusCounts.put(row[0].toString(), (Long) row[1]);
        }
        long totalRecords  = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        long deducted      = statusCounts.getOrDefault(MaintenanceFeeStatus.DEDUCTED.name(), 0L);
        long partial       = statusCounts.getOrDefault(MaintenanceFeeStatus.PARTIAL.name(), 0L);
        long onDebt        = statusCounts.getOrDefault(MaintenanceFeeStatus.DEBT.name(), 0L);
        long failed        = statusCounts.getOrDefault(MaintenanceFeeStatus.FAILED.name(), 0L);
        long waived        = statusCounts.getOrDefault(MaintenanceFeeStatus.WAIVED.name(), 0L);
        long repaid        = statusCounts.getOrDefault(MaintenanceFeeStatus.REPAID.name(), 0L);
        long pending       = statusCounts.getOrDefault(MaintenanceFeeStatus.PENDING.name(), 0L);

        BigDecimal totalDeducted    = feeTransactionRepo.sumDeductedForMonth(month);
        BigDecimal totalOutstanding = feeTransactionRepo.sumOutstandingDebtForMonth(month);

        List<Map<String, Object>> debtByCurrency = debtRepo.sumActiveDebtByCurrency()
                .stream().map(r -> Map.<String, Object>of(
                        "currency", r[0],
                        "totalOutstandingDebt", r[1]))
                .collect(Collectors.toList());

        Map<String, Long> debtStatusCounts = new LinkedHashMap<>();
        for (Object[] row : debtRepo.countByStatus()) {
            debtStatusCounts.put(row[0].toString(), (Long) row[1]);
        }
        long usersWithActiveDebt = debtRepo.countUsersWithActiveDebt();

        return DashboardOverviewDto.builder()
                .billingMonth(month)
                .totalRecordsForMonth(totalRecords)
                .fullyDeducted(deducted)
                .partiallyDeducted(partial)
                .newDebts(onDebt)
                .failed(failed)
                .waived(waived)
                .repaid(repaid)
                .pending(pending)
                .totalAmountDeductedForMonth(totalDeducted)
                .totalOutstandingDebtForMonth(totalOutstanding)
                .usersWithActiveDebt(usersWithActiveDebt)
                .debtByCurrency(debtByCurrency)
                .debtLedgerStatusCounts(debtStatusCounts)
                .availableMonths(feeTransactionRepo.findDistinctMonths())
                .activeConfigs(configService.getAllActive())
                .build();
    }


    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public UserMaintenanceProfileDto getUserProfile(Long userId) {
        String email    = userLookupPort.findEmailByUserId(userId).orElse("unknown");
        String fullName = userLookupPort.findFullNameByUserId(userId).orElse("Unknown User");

        List<UserDebtDto> activeDebts = debtRepo.findActiveDebtsByUserId(userId)
                .stream().map(this::toDebtDto).collect(Collectors.toList());

        List<MaintenanceFeeTransaction> allTxns =
                feeTransactionRepo.findActiveDebtsByUserId(userId); 

        Page<MaintenanceFeeTransaction> fullHistory =
                feeTransactionRepo.findByUserId(userId, Pageable.unpaged());
        List<MaintenanceFeeTransactionDto> feeHistory = fullHistory.getContent()
                .stream().map(this::toFeeDto).collect(Collectors.toList());

        List<UserMonthlyActivityDto> activityHistory = activityRepo.findByUserId(userId)
                .stream().map(this::toActivityDto).collect(Collectors.toList());

        DebtAgingInfo debtAging = computeDebtAging(userId);

        BigDecimal totalFeesPaidLifetime = fullHistory.getContent().stream()
                .map(MaintenanceFeeTransaction::getDeductedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstandingDebt = activeDebts.stream()
                .map(UserDebtDto::getTotalDebt)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return UserMaintenanceProfileDto.builder()
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .activeDebts(activeDebts)
                .feeHistory(feeHistory)
                .activityHistory(activityHistory)
                .debtAging(debtAging)
                .totalFeesPaidLifetime(totalFeesPaidLifetime)
                .totalOutstandingDebt(totalOutstandingDebt)
                .hasActiveDebt(!activeDebts.isEmpty())
                .build();
    }

    public DebtAgingReportDto getDebtAgingReport() {
        LocalDateTime now = LocalDateTime.now();
        List<UserDebt> allDebts = debtRepo.findAll().stream()
                .filter(d -> d.getStatus() != DebtStatus.SETTLED)
                .collect(Collectors.toList());

        List<DebtAgingEntryDto> bucket0to30  = new ArrayList<>();
        List<DebtAgingEntryDto> bucket31to60 = new ArrayList<>();
        List<DebtAgingEntryDto> bucket61to90 = new ArrayList<>();
        List<DebtAgingEntryDto> bucket90plus = new ArrayList<>();

        for (UserDebt debt : allDebts) {
            long ageInDays = ChronoUnit.DAYS.between(debt.getCreatedAt(), now);
            String email    = userLookupPort.findEmailByUserId(debt.getUserId()).orElse("unknown");
            String fullName = userLookupPort.findFullNameByUserId(debt.getUserId()).orElse("Unknown");

            DebtAgingEntryDto entry = DebtAgingEntryDto.builder()
                    .userId(debt.getUserId())
                    .email(email)
                    .fullName(fullName)
                    .currencyCode(debt.getCurrencyCode())
                    .totalDebt(debt.getTotalDebt())
                    .debtStatus(debt.getStatus())
                    .debtCreatedAt(debt.getCreatedAt())
                    .ageInDays(ageInDays)
                    .lastActivityDate(debt.getLastActivityDate())
                    .build();

            if      (ageInDays <= 30)  bucket0to30.add(entry);
            else if (ageInDays <= 60)  bucket31to60.add(entry);
            else if (ageInDays <= 90)  bucket61to90.add(entry);
            else                       bucket90plus.add(entry);
        }

        return DebtAgingReportDto.builder()
                .generatedAt(now)
                .totalActiveDebts(allDebts.size())
                .bucket0to30Days(bucket0to30)
                .bucket31to60Days(bucket31to60)
                .bucket61to90Days(bucket61to90)
                .bucket90PlusDays(bucket90plus)
                .totalDebtValue(allDebts.stream().map(UserDebt::getTotalDebt)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .build();
    }

    public MonthlyStatusReportDto getMonthlyStatusReport(LocalDate billingMonth) {
        LocalDate month = billingMonth.withDayOfMonth(1);

        Map<String, Long> countsByStatus = new LinkedHashMap<>();
        for (Object[] row : feeTransactionRepo.countByStatusForMonth(month)) {
            countsByStatus.put(row[0].toString(), (Long) row[1]);
        }

        BigDecimal totalDeducted    = feeTransactionRepo.sumDeductedForMonth(month);
        BigDecimal totalOutstanding = feeTransactionRepo.sumOutstandingDebtForMonth(month);

        return MonthlyStatusReportDto.builder()
                .billingMonth(month)
                .statusBreakdown(countsByStatus)
                .totalAmountDeducted(totalDeducted)
                .totalOutstandingDebt(totalOutstanding)
                .build();
    }

    public Page<AuditLogDto> getAuditLogs(String batchId, Long userId, Pageable pageable) {
        Page<MaintenanceFeeAuditLog> page;
        if (batchId != null && !batchId.isBlank()) {
            List<MaintenanceFeeAuditLog> list = auditLogRepo.findByBatchId(batchId);
            page = new PageImpl<>(list, pageable, list.size());
        } else if (userId != null) {
            page = auditLogRepo.findByUserId(userId, pageable);
        } else {
            page = auditLogRepo.findAllPaged(pageable);
        }
        return page.map(this::toAuditDto);
    }

    public List<Map<String, Object>> getBatchStats(String batchId) {
        return auditLogRepo.countByActionForBatch(batchId).stream()
                .map(r -> Map.<String, Object>of("action", r[0], "count", r[1]))
                .collect(Collectors.toList());
    }

    private DebtAgingInfo computeDebtAging(Long userId) {
        List<UserDebt> debts = debtRepo.findActiveDebtsByUserId(userId);
        if (debts.isEmpty()) return null;
        LocalDateTime now = LocalDateTime.now();
        UserDebt oldest = debts.stream()
                .min(Comparator.comparing(UserDebt::getCreatedAt))
                .orElse(debts.get(0));
        long days = ChronoUnit.DAYS.between(oldest.getCreatedAt(), now);
        String bracket;
        if      (days <= 30) bracket = "0-30 days";
        else if (days <= 60) bracket = "31-60 days";
        else if (days <= 90) bracket = "61-90 days";
        else                 bracket = "90+ days (escalate)";
        return DebtAgingInfo.builder()
                .oldestDebtDate(oldest.getCreatedAt())
                .ageInDays(days)
                .ageBracket(bracket)
                .build();
    }

    public MaintenanceFeeTransactionDto toFeeDto(MaintenanceFeeTransaction t) {
        return MaintenanceFeeTransactionDto.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .currencyCode(t.getCurrencyCode())
                .monthYear(t.getMonthYear())
                .feeAmount(t.getFeeAmount())
                .feeType(t.getFeeType())
                .status(t.getStatus())
                .deductedAmount(t.getDeductedAmount())
                .walletBalanceBefore(t.getWalletBalanceBefore())
                .walletBalanceAfter(t.getWalletBalanceAfter())
                .debtAmount(t.getDebtAmount())
                .repaid(t.isRepaid())
                .repaidAt(t.getRepaidAt())
                .referenceId(t.getReferenceId())
                .adminNotes(t.getAdminNotes())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    public UserDebtDto toDebtDto(UserDebt d) {
        return UserDebtDto.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .currencyCode(d.getCurrencyCode())
                .totalDebt(d.getTotalDebt())
                .totalRepaid(d.getTotalRepaid())
                .status(d.getStatus())
                .lastActivityDate(d.getLastActivityDate())
                .settledAt(d.getSettledAt())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    public UserMonthlyActivityDto toActivityDto(UserMonthlyActivity a) {
        return UserMonthlyActivityDto.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .currencyCode(a.getCurrencyCode())
                .monthYear(a.getMonthYear())
                .transactionCount(a.getTransactionCount())
                .totalVolume(a.getTotalVolume())
                .hasActivity(a.isHasActivity())
                .processed(a.isProcessed())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private AuditLogDto toAuditDto(MaintenanceFeeAuditLog l) {
        return AuditLogDto.builder()
                .id(l.getId())
                .batchId(l.getBatchId())
                .userId(l.getUserId())
                .action(l.getAction() != null ? l.getAction().name() : null)
                .status(l.getStatus() != null ? l.getStatus().name() : null)
                .details(l.getDetails())
                .errorMessage(l.getErrorMessage())
                .createdAt(l.getCreatedAt())
                .build();
    }

    @Data @Builder
    public static class DashboardOverviewDto {
        private LocalDate billingMonth;
        private long totalRecordsForMonth;
        private long fullyDeducted;
        private long partiallyDeducted;
        private long newDebts;
        private long failed;
        private long waived;
        private long repaid;
        private long pending;
        private BigDecimal totalAmountDeductedForMonth;
        private BigDecimal totalOutstandingDebtForMonth;
        private long usersWithActiveDebt;
        private List<Map<String, Object>> debtByCurrency;
        private Map<String, Long> debtLedgerStatusCounts;
        private List<LocalDate> availableMonths;
        private List<MaintenanceFeeConfigDto> activeConfigs;
    }

    @Data @Builder
    public static class UserMaintenanceProfileDto {
        private Long userId;
        private String email;
        private String fullName;
        private boolean hasActiveDebt;
        private BigDecimal totalOutstandingDebt;
        private BigDecimal totalFeesPaidLifetime;
        private List<UserDebtDto> activeDebts;
        private List<MaintenanceFeeTransactionDto> feeHistory;
        private List<UserMonthlyActivityDto> activityHistory;
        private DebtAgingInfo debtAging;
    }

    @Data @Builder
    public static class DebtAgingInfo {
        private LocalDateTime oldestDebtDate;
        private long ageInDays;
        private String ageBracket;  
    }

    @Data @Builder
    public static class DebtAgingReportDto {
        private LocalDateTime generatedAt;
        private int totalActiveDebts;
        private BigDecimal totalDebtValue;
        private List<DebtAgingEntryDto> bucket0to30Days;
        private List<DebtAgingEntryDto> bucket31to60Days;
        private List<DebtAgingEntryDto> bucket61to90Days;
        private List<DebtAgingEntryDto> bucket90PlusDays;
    }

    @Data @Builder
    public static class DebtAgingEntryDto {
        private Long userId;
        private String email;
        private String fullName;
        private String currencyCode;
        private BigDecimal totalDebt;
        private DebtStatus debtStatus;
        private LocalDateTime debtCreatedAt;
        private long ageInDays;
        private LocalDateTime lastActivityDate;
    }

    @Data @Builder
    public static class MonthlyStatusReportDto {
        private LocalDate billingMonth;
        private Map<String, Long> statusBreakdown;
        private BigDecimal totalAmountDeducted;
        private BigDecimal totalOutstandingDebt;
    }

    @Data @Builder
    public static class AuditLogDto {
        private Long id;
        private String batchId;
        private Long userId;
        private String action;
        private String status;
        private String details;
        private String errorMessage;
        private LocalDateTime createdAt;
    }
}
