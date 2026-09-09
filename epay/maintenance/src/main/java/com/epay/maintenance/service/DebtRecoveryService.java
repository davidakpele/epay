package com.epay.maintenance.service;

import com.epay.common.interfaces.IDebtRecoveryPort;
import com.epay.common.interfaces.IHistoryPort;
import com.epay.common.interfaces.IWalletNotificationPublisher;
import com.epay.common.interfaces.UserLookupPort;
import com.epay.domain.maintenance.entity.MaintenanceFeeTransaction;
import com.epay.domain.maintenance.entity.UserDebt;
import com.epay.domain.maintenance.enums.DebtStatus;
import com.epay.domain.maintenance.enums.MaintenanceAuditAction;
import com.epay.domain.maintenance.enums.MaintenanceFeeStatus;
import com.epay.maintenance.repository.MaintenanceFeeAuditLogRepository;
import com.epay.maintenance.repository.MaintenanceFeeTransactionRepository;
import com.epay.maintenance.repository.UserDebtRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebtRecoveryService implements IDebtRecoveryPort {

    private final UserDebtRepository                 debtRepository;
    private final MaintenanceFeeTransactionRepository feeTransactionRepository;
    private final MaintenanceFeeAuditLogRepository   auditLogRepository;
    private final UserLookupPort                     userLookupPort;
    private final IHistoryPort                       historyPort;
    private final IWalletNotificationPublisher       notificationPublisher;
    private final ObjectMapper                       objectMapper;
    private final MaintenanceFeeEngine               feeEngine;

    @Override
    @Transactional
    public BigDecimal recoverOnCredit(Long userId, String currencyCode,
                                      BigDecimal incomingAmount, Long walletId) {
        String code = currencyCode.toUpperCase();

        UserDebt debtLedger = debtRepository.findByUserIdAndCurrency(userId, code)
                .filter(d -> d.getStatus() != DebtStatus.SETTLED
                        && d.getTotalDebt().compareTo(BigDecimal.ZERO) > 0)
                .orElse(null);

        if (debtLedger == null) {
            return incomingAmount;  
        }

        BigDecimal totalDebt      = debtLedger.getTotalDebt();
        BigDecimal recovered      = incomingAmount.min(totalDebt); 
        BigDecimal netCredit      = incomingAmount.subtract(recovered);
        BigDecimal remainingDebt  = totalDebt.subtract(recovered);
        boolean    fullySettled   = remainingDebt.compareTo(BigDecimal.ZERO) <= 0;

        LocalDateTime now = LocalDateTime.now();

        applyRecoveryToFeeTransactions(userId, code, recovered);

        if (fullySettled) {
            debtRepository.markSettled(debtLedger.getId(), now);
        } else {
            debtRepository.reduceDebt(debtLedger.getId(), recovered, now);
            debtLedger.setStatus(DebtStatus.PARTIAL);
            debtRepository.save(debtLedger);
        }

        String recoverRef = "DEBT-RCVR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase();
        String fullName   = userLookupPort.findFullNameByUserId(userId).orElse("Account Holder");
        try {
            historyPort.record(userId, walletId,
                    UUID.randomUUID().toString(), recoverRef,
                    "FEE", "DEBIT", "SYSTEM", "SUCCESS",
                    recovered, recovered, recovered,
                    incomingAmount, netCredit, code, code, fullName,
                    "MAINTENANCE DEBT RECOVERY",
                    null, null, null, null, null, null,
                    "Maintenance fee debt recovered from incoming credit", now);
        } catch (Exception ex) {
            log.warn("[DebtRecovery] History failed userId={}: {}", userId, ex.getMessage());
        }

        MaintenanceAuditAction auditAction = fullySettled
                ? MaintenanceAuditAction.DEBT_REPAID
                : MaintenanceAuditAction.DEBT_PARTIAL_REPAID;
        audit("RECOVERY", userId, auditAction,
                Map.of("currency", code, "incoming", incomingAmount,
                        "recovered", recovered, "remaining", remainingDebt,
                        "fullySettled", fullySettled));

        String userEmail  = userLookupPort.findEmailByUserId(userId).orElse(null);
        String[] names    = splitName(fullName);
        if (userEmail != null) {
            try {
                notificationPublisher.publishDebtRepaidNotification(
                        userEmail, names[0], names[1], userId, code,
                        recovered, remainingDebt, netCredit, fullySettled);
            } catch (Exception ex) {
                log.warn("[DebtRecovery] Notification failed userId={}: {}", userId, ex.getMessage());
            }
        }

        log.info("[DebtRecovery] userId={} currency={} recovered={} remaining={} settled={}",
                userId, code, recovered, remainingDebt, fullySettled);

        return netCredit;
    }

    private void applyRecoveryToFeeTransactions(Long userId, String code, BigDecimal toRecover) {
        List<MaintenanceFeeTransaction> debts =
                feeTransactionRepository.findActiveDebtsByUserIdAndCurrency(userId, code);

        BigDecimal remaining = toRecover;
        for (MaintenanceFeeTransaction txn : debts) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal apply = remaining.min(txn.getDebtAmount());
            BigDecimal newDebt = txn.getDebtAmount().subtract(apply);
            remaining = remaining.subtract(apply);

            txn.setDebtAmount(newDebt);
            if (newDebt.compareTo(BigDecimal.ZERO) <= 0) {
                txn.setRepaid(true);
                txn.setRepaidAt(LocalDateTime.now());
                txn.setStatus(MaintenanceFeeStatus.REPAID);
            } else {
                txn.setStatus(MaintenanceFeeStatus.PARTIAL);
            }
            feeTransactionRepository.save(txn);
        }
    }

    private void audit(String batchId, Long userId,
                       MaintenanceAuditAction action, Map<?, ?> details) {
        try {
            String json = objectMapper.writeValueAsString(details);
            com.epay.domain.maintenance.entity.MaintenanceFeeAuditLog entry =
                    com.epay.domain.maintenance.entity.MaintenanceFeeAuditLog.builder()
                            .batchId(batchId)
                            .userId(userId)
                            .action(action)
                            .details(json)
                            .build();
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.warn("[DebtRecovery] Audit failed: {}", ex.getMessage());
        }
    }

    private String[] splitName(String fullName) {
        if (fullName == null) return new String[]{"Valued", "Customer"};
        int space = fullName.indexOf(' ');
        if (space < 0) return new String[]{fullName, ""};
        return new String[]{fullName.substring(0, space), fullName.substring(space + 1)};
    }
}
