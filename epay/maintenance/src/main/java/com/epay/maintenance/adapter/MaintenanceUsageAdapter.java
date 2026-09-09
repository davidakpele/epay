package com.epay.maintenance.adapter;

import com.epay.common.interfaces.IMaintenanceUsagePort;
import com.epay.domain.maintenance.entity.UserMonthlyActivity;
import com.epay.maintenance.repository.UserDebtRepository;
import com.epay.maintenance.repository.UserMonthlyActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;


@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceUsageAdapter implements IMaintenanceUsagePort {

    private final UserMonthlyActivityRepository activityRepository;
    private final UserDebtRepository            debtRepository;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordActivity(Long userId, String currencyCode,
                               BigDecimal amount, String transactionType) {
        if (userId == null || currencyCode == null || amount == null) return;
        String code = currencyCode.toUpperCase();
        LocalDate monthYear = LocalDate.now().withDayOfMonth(1);
        try {
            int updated = activityRepository.incrementActivity(userId, code, monthYear, amount);
            if (updated == 0) {
                insertNewActivityRow(userId, code, monthYear, amount);
            }
        } catch (Exception ex) {
            try {
                activityRepository.incrementActivity(userId, code, monthYear, amount);
            } catch (Exception retryEx) {
                log.warn("[MaintenanceUsage] Retry failed userId={} currency={}: {}",
                        userId, code, retryEx.getMessage());
            }
        }
        log.debug("[MaintenanceUsage] Recorded: userId={} currency={} amount={} type={}",
                userId, code, amount, transactionType);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void insertNewActivityRow(Long userId, String code,
                                        LocalDate monthYear, BigDecimal amount) {
        UserMonthlyActivity activity = UserMonthlyActivity.builder()
                .userId(userId)
                .currencyCode(code)
                .monthYear(monthYear)
                .transactionCount(1)
                .totalVolume(amount)
                .hasActivity(true)
                .processed(false)
                .build();
        activityRepository.save(activity);
    }

    @Override
    public boolean hasActiveDebt(Long userId, String currencyCode) {
        if (userId == null || currencyCode == null) return false;
        try {
            return debtRepository.hasActiveDebt(userId, currencyCode.toUpperCase());
        } catch (Exception ex) {
            log.warn("[MaintenanceUsage] hasActiveDebt check failed userId={}: {}", userId, ex.getMessage());
            return false;  
        }
    }
    @Override
    public BigDecimal recoverDebtOnCredit(Long userId, String currencyCode,
                                          BigDecimal incomingAmount, Long walletId) {
        return incomingAmount;
    }
}
