package com.epay.maintenance.service;

import com.epay.common.interfaces.IHistoryPort;
import com.epay.common.interfaces.IWalletNotificationPublisher;
import com.epay.common.interfaces.UserLookupPort;
import com.epay.domain.maintenance.entity.*;
import com.epay.domain.maintenance.enums.*;
import com.epay.domain.maintenance.input.WaiveFeeRequest;
import com.epay.domain.wallet.entity.CurrencyBalance;
import com.epay.domain.wallet.entity.Wallet;
import com.epay.maintenance.repository.*;
import com.epay.wallet.cache.WalletCacheService;
import com.epay.wallet.repository.CurrencyBalanceRepository;
import com.epay.wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceFeeEngine {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMMM yyyy");

    private final MaintenanceFeeConfigRepository     configRepository;
    private final MaintenanceFeeTransactionRepository feeTransactionRepository;
    private final UserDebtRepository                 debtRepository;
    private final MaintenanceFeeAuditLogRepository   auditLogRepository;
    private final WalletRepository                   walletRepository;
    private final CurrencyBalanceRepository          currencyBalanceRepository;
    private final WalletCacheService                 walletCacheService;
    private final UserLookupPort                     userLookupPort;
    private final IHistoryPort                       historyPort;
    private final IWalletNotificationPublisher       notificationPublisher;
    private final ObjectMapper                       objectMapper;

    @Transactional
    public MaintenanceFeeStatus charge(Long userId, String currencyCode,
                                       LocalDate billingMonth, String batchId) {
        String code = currencyCode.toUpperCase();


        if (feeTransactionRepository.existsBillForMonth(userId, code, billingMonth)) {
            log.debug("[FeeEngine] Already billed userId={} currency={} month={}", userId, code, billingMonth);
            return MaintenanceFeeStatus.DEDUCTED; 
        }

        Optional<MaintenanceFeeConfig> configOpt = configRepository.findActiveByCurrencyCode(code);
        if (configOpt.isEmpty()) {
            audit(batchId, userId, MaintenanceAuditAction.FAILED, null,
                    MaintenanceFeeStatus.FAILED, "No active fee config for " + code);
            log.warn("[FeeEngine] No fee config for currency={}", code);
            return MaintenanceFeeStatus.FAILED;
        }

        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);
        if (walletOpt.isEmpty() || !walletOpt.get().isActive()) {
            audit(batchId, userId, MaintenanceAuditAction.FAILED, null,
                    MaintenanceFeeStatus.FAILED, "Wallet missing or inactive for userId=" + userId);
            return MaintenanceFeeStatus.FAILED;
        }
        Wallet wallet = walletOpt.get();

        Optional<CurrencyBalance> balanceOpt =
                currencyBalanceRepository.findByWalletIdAndCurrencyCode(wallet.getId(), code);
        if (balanceOpt.isEmpty()) {
            return MaintenanceFeeStatus.FAILED;
        }
        CurrencyBalance balance = balanceOpt.get();

        MaintenanceFeeConfig config = configOpt.get();
        BigDecimal feeAmount = calculateFee(config, balance);
        if (feeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return MaintenanceFeeStatus.DEDUCTED; 
        }

        String userEmail     = userLookupPort.findEmailByUserId(userId).orElse(null);
        String fullName      = userLookupPort.findFullNameByUserId(userId).orElse("Valued Customer");
        String[] names       = splitName(fullName);
        String billingLabel  = billingMonth.format(MONTH_FMT);

        BigDecimal availableBalance = balance.getBalance();
        BigDecimal deductable       = availableBalance.min(feeAmount); 
        BigDecimal debtAmount       = feeAmount.subtract(deductable);
        BigDecimal newBalance       = availableBalance.subtract(deductable);

        MaintenanceFeeStatus outcome;
        if (deductable.compareTo(feeAmount) >= 0) {
            outcome = MaintenanceFeeStatus.DEDUCTED;
        } else if (deductable.compareTo(BigDecimal.ZERO) > 0) {
            outcome = MaintenanceFeeStatus.PARTIAL;
        } else {
            outcome = MaintenanceFeeStatus.DEBT;
        }

        String referenceId = "MAINT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();

        MaintenanceFeeTransaction feeTxn = MaintenanceFeeTransaction.builder()
                .userId(userId)
                .currencyCode(code)
                .monthYear(billingMonth)
                .feeAmount(feeAmount)
                .feeType(config.getFeeType())
                .status(outcome)
                .deductedAmount(deductable)
                .walletBalanceBefore(availableBalance)
                .walletBalanceAfter(newBalance)
                .debtAmount(debtAmount)
                .repaid(false)
                .referenceId(referenceId)
                .batchId(batchId)
                .build();
        feeTransactionRepository.save(feeTxn);

        if (deductable.compareTo(BigDecimal.ZERO) > 0) {
            currencyBalanceRepository.updateBalance(balance.getId(), newBalance);
            walletCacheService.updateBalance(userId, code, newBalance, newBalance,
                    UUID.randomUUID().toString());

            try {
                historyPort.record(userId, wallet.getId(),
                        UUID.randomUUID().toString(), referenceId,
                        "FEE", "DEBIT", "SYSTEM", "SUCCESS",
                        deductable, deductable, deductable,
                        availableBalance, newBalance, code, code, fullName,
                        "MONTHLY MAINTENANCE FEE — " + billingLabel,
                        null, null, null, null, null, null,
                        "Monthly maintenance fee for " + code, LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("[FeeEngine] History record failed userId={} currency={}: {}", userId, code, ex.getMessage());
            }
        }

        if (debtAmount.compareTo(BigDecimal.ZERO) > 0) {
            updateDebtLedger(userId, code, debtAmount);
        }

        MaintenanceAuditAction auditAction = switch (outcome) {
            case DEDUCTED -> MaintenanceAuditAction.DEDUCTED;
            case PARTIAL  -> MaintenanceAuditAction.PARTIAL_DEDUCTED;
            case DEBT     -> MaintenanceAuditAction.DEBT_CREATED;
            default       -> MaintenanceAuditAction.FAILED;
        };
        audit(batchId, userId, auditAction,
                Map.of("currency", code, "feeAmount", feeAmount,
                        "deducted", deductable, "debt", debtAmount,
                        "balanceBefore", availableBalance, "balanceAfter", newBalance,
                        "billingMonth", billingLabel),
                outcome, null);

        notifyUser(outcome, userId, userEmail, names[0], names[1], code,
                feeAmount, deductable, debtAmount, newBalance, availableBalance, billingLabel);

        log.info("[FeeEngine] userId={} currency={} outcome={} fee={} deducted={} debt={}",
                userId, code, outcome, feeAmount, deductable, debtAmount);

        return outcome;
    }

    @Transactional
    public void waive(WaiveFeeRequest request, Long adminId) {
        MaintenanceFeeTransaction txn = feeTransactionRepository.findById(request.getFeeTransactionId())
                .orElseThrow(() -> new com.epay.common.exception.ResourceNotFoundException(
                        "Fee transaction not found: " + request.getFeeTransactionId()));

        if (txn.getDebtAmount().compareTo(BigDecimal.ZERO) > 0) {
            debtRepository.findByUserIdAndCurrency(txn.getUserId(), txn.getCurrencyCode())
                    .ifPresent(debt -> {
                        BigDecimal newTotal = debt.getTotalDebt().subtract(txn.getDebtAmount())
                                .max(BigDecimal.ZERO);
                        debt.setTotalDebt(newTotal);
                        debt.setStatus(newTotal.compareTo(BigDecimal.ZERO) == 0
                                ? DebtStatus.SETTLED : debt.getStatus());
                        debtRepository.save(debt);
                    });
        }

        txn.setStatus(MaintenanceFeeStatus.WAIVED);
        txn.setDebtAmount(BigDecimal.ZERO);
        txn.setAdminNotes("Waived by admin " + adminId + ": " + request.getReason());
        feeTransactionRepository.save(txn);

        audit("ADMIN", txn.getUserId(), MaintenanceAuditAction.WAIVED,
                Map.of("feeTransactionId", txn.getId(), "reason", request.getReason(), "adminId", adminId),
                MaintenanceFeeStatus.WAIVED, null);

        log.info("[FeeEngine] Waived feeTxnId={} by adminId={}", txn.getId(), adminId);
    }

    private BigDecimal calculateFee(MaintenanceFeeConfig config, CurrencyBalance balance) {
        BigDecimal fee;
        if (config.getFeeType() == FeeType.FIXED) {
            fee = config.getFeeAmount();
        } else {

            fee = balance.getBalance()
                    .multiply(config.getFeePercentage())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            if (config.getMinimumFee() != null)
                fee = fee.max(config.getMinimumFee());
            if (config.getMaximumFee() != null)
                fee = fee.min(config.getMaximumFee());
        }
        return fee.setScale(4, RoundingMode.HALF_UP);
    }

    private void updateDebtLedger(Long userId, String currencyCode, BigDecimal debtAmount) {
        LocalDateTime now = LocalDateTime.now();
        debtRepository.findByUserIdAndCurrency(userId, currencyCode)
                .ifPresentOrElse(
                        existing -> {
                            debtRepository.addDebt(existing.getId(), debtAmount, now);
                        },
                        () -> {
                            UserDebt newDebt = UserDebt.builder()
                                    .userId(userId)
                                    .currencyCode(currencyCode)
                                    .totalDebt(debtAmount)
                                    .totalRepaid(BigDecimal.ZERO)
                                    .status(DebtStatus.ACTIVE)
                                    .lastActivityDate(now)
                                    .build();
                            debtRepository.save(newDebt);
                        });
    }

    private void notifyUser(MaintenanceFeeStatus outcome, Long userId,
                            String userEmail, String firstName, String lastName,
                            String currency, BigDecimal feeAmount, BigDecimal deducted,
                            BigDecimal debt, BigDecimal newBalance, BigDecimal prevBalance,
                            String billingLabel) {
        if (userEmail == null) return;
        try {
            if (outcome == MaintenanceFeeStatus.DEDUCTED) {
                notificationPublisher.publishMaintenanceNotification(
                        "FEE_DEDUCTED", newBalance, currency, feeAmount,
                        prevBalance, "Monthly maintenance fee — " + billingLabel,
                        true, OffsetDateTime.now(), feeAmount,
                        userEmail, firstName, userId, lastName);
            } else {
                notificationPublisher.publishDebtCreatedNotification(
                        userEmail, firstName, lastName, userId, currency,
                        feeAmount, deducted, debt, newBalance, billingLabel);
            }
        } catch (Exception ex) {
            log.warn("[FeeEngine] Notification failed userId={}: {}", userId, ex.getMessage());
        }
    }

    private void audit(String batchId, Long userId, MaintenanceAuditAction action,
                       Map<?, ?> details, MaintenanceFeeStatus status, String errorMessage) {
        try {
            String detailsJson = details != null ? objectMapper.writeValueAsString(details) : null;
            MaintenanceFeeAuditLog log = MaintenanceFeeAuditLog.builder()
                    .batchId(batchId)
                    .userId(userId)
                    .action(action)
                    .details(detailsJson)
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();
            auditLogRepository.save(log);
        } catch (Exception ex) {
            MaintenanceFeeEngine.log.warn("[FeeEngine] Audit log failed: {}", ex.getMessage());
        }
    }

    private String[] splitName(String fullName) {
        if (fullName == null) return new String[]{"Valued", "Customer"};
        int space = fullName.indexOf(' ');
        if (space < 0) return new String[]{fullName, ""};
        return new String[]{fullName.substring(0, space), fullName.substring(space + 1)};
    }
}
