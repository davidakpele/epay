package com.example.admin_api_service.services;

import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.example.admin_api_service.components.LiquidityAlertTriggeredEvent;
import com.example.admin_api_service.enums.AlertStatus;
import com.example.admin_api_service.models.LiquidityThresholdAlert;
import com.example.admin_api_service.models.SystemWallet;
import com.example.admin_api_service.repository.LiquidityThresholdAlertRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LiquidityAlertService {

    private final LiquidityThresholdAlertRepository alertRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void evaluateThreshold(SystemWallet wallet) {
        if (!wallet.isBelowThreshold()) return;

        // Avoid duplicate active alerts for the same wallet
        boolean hasActiveAlert = alertRepository
                .findBySystemWalletIdAndStatus(wallet.getId(), AlertStatus.ACTIVE)
                .stream().findAny().isPresent();

        if (hasActiveAlert) return;

        LiquidityThresholdAlert alert = LiquidityThresholdAlert.builder()
                .systemWallet(wallet)
                .currency(wallet.getCurrency())
                .balanceAtTrigger(wallet.getBalance())
                .thresholdAtTrigger(wallet.getMinimumThreshold())
                .thresholdValue(wallet.getMinimumThreshold())
                .availableBalance(wallet.getAvailableBalance())
                .totalLiabilities(wallet.getTotalUserLiabilities())
                .reserveRatio(wallet.getReserveRatio())
                .message(buildAlertMessage(wallet))
                .build();

        alertRepository.save(alert);
        log.warn("Liquidity alert triggered: currency={}, available={}, threshold={}",
                wallet.getCurrency(), wallet.getAvailableBalance(), wallet.getMinimumThreshold());

        eventPublisher.publishEvent(new LiquidityAlertTriggeredEvent(alert));
    }

    public void evaluateAndResolveAlerts(SystemWallet wallet, Long adminId) {
        if (wallet.isBelowThreshold()) return;

        List<LiquidityThresholdAlert> active = alertRepository
                .findBySystemWalletIdAndStatus(wallet.getId(), AlertStatus.ACTIVE);

        active.forEach(alert -> {
            alert.resolve();
            alert.setResolvedByAdminId(adminId);
        });
        alertRepository.saveAll(active);

        if (!active.isEmpty()) {
            log.info("Resolved {} alert(s) for currency={}", active.size(), wallet.getCurrency());
        }
    }

    public LiquidityThresholdAlert resolveAlertManually(String alertId, Long adminId) {
        LiquidityThresholdAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new EntityNotFoundException("Alert not found: " + alertId));
        alert.resolve();
        alert.setResolvedByAdminId(adminId);
        return alertRepository.save(alert);
    }

    @Transactional
    public List<LiquidityThresholdAlert> getActiveAlerts() {
        return alertRepository.findByStatus(AlertStatus.ACTIVE);
    }

    @Transactional
    public List<LiquidityThresholdAlert> getAlertsByWallet(String walletId) {
        return alertRepository.findBySystemWalletId(walletId);
    }

    private String buildAlertMessage(SystemWallet wallet) {
        return String.format(
            "ALERT: %s wallet available balance (%.4f) has dropped below minimum threshold (%.4f). Reserve ratio: %.4f",
            wallet.getCurrency(), wallet.getAvailableBalance(),
            wallet.getMinimumThreshold(), wallet.getReserveRatio());
    }
}
