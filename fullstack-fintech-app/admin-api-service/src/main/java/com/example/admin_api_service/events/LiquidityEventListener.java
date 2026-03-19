package com.example.admin_api_service.events;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.example.admin_api_service.components.LiquidityAlertResolvedEvent;
import com.example.admin_api_service.components.LiquidityAlertTriggeredEvent;
import com.example.admin_api_service.components.WalletFundedEvent;
import com.example.admin_api_service.components.WalletRebalancedEvent;
import com.example.admin_api_service.components.WalletWithdrawnEvent;
import com.example.admin_api_service.enums.AuditAction;
import com.example.admin_api_service.enums.NotificationPriority;
import com.example.admin_api_service.models.LiquidityThresholdAlert;
import com.example.admin_api_service.services.AuditLogService;
import com.example.admin_api_service.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class LiquidityEventListener {

    private final NotificationService notificationService; 
    private final AuditLogService auditLogService; 

    @EventListener
    @Async  
    public void onWalletFunded(WalletFundedEvent event) {
        log.info("[LIQUIDITY] Wallet funded — currency={}, amount={}, adminId={}",
                event.wallet().getCurrency(),
                event.amount(),
                event.adminId());

        auditLogService.log(
                AuditAction.WALLET_FUNDED,
                event.adminId(),
                "Funded %s wallet with %s".formatted(event.wallet().getCurrency(), event.amount())
        );
    }

    @EventListener
    @Async
    public void onWalletWithdrawn(WalletWithdrawnEvent event) {
        log.info("[LIQUIDITY] Wallet withdrawn — currency={}, amount={}, adminId={}",
                event.wallet().getCurrency(),
                event.amount(),
                event.adminId());

        auditLogService.log(
                AuditAction.WALLET_WITHDRAWN,
                event.adminId(),
                "Withdrew %s from %s wallet".formatted(event.amount(), event.wallet().getCurrency())
        );
    }

    @EventListener
    @Async
    public void onWalletRebalanced(WalletRebalancedEvent event) {
        log.info("[LIQUIDITY] Rebalance — from={}, to={}, amount={}, adminId={}",
                event.fromWallet().getCurrency(),
                event.toWallet().getCurrency(),
                event.amount(),
                event.adminId());

        auditLogService.log(
                AuditAction.WALLET_REBALANCED,
                event.adminId(),
                "Rebalanced %s from %s to %s".formatted(
                        event.amount(),
                        event.fromWallet().getCurrency(),
                        event.toWallet().getCurrency())
        );
    }

    @EventListener
    @Async
    public void onAlertTriggered(LiquidityAlertTriggeredEvent event) {
        LiquidityThresholdAlert alert = event.alert();

        log.warn("[LIQUIDITY] Threshold alert triggered — currency={}, available={}, threshold={}",
                alert.getCurrency(),
                alert.getAvailableBalance(),
                alert.getThresholdAtTrigger());

        // Push an in-app notification to all admins
        notificationService.notifyAdmins(
                NotificationPriority.HIGH,
                "Low Liquidity Alert",
                alert.getMessage()
        );

        auditLogService.log(
                AuditAction.LIQUIDITY_ALERT_TRIGGERED,
                null,
                alert.getMessage()
        );
    }

    @EventListener
    @Async
    public void onAlertResolved(LiquidityAlertResolvedEvent event) {
        LiquidityThresholdAlert alert = event.alert();

        log.info("[LIQUIDITY] Alert resolved — currency={}, resolvedBy={}",
                alert.getCurrency(),
                event.resolvedByAdminId());

        notificationService.notifyAdmins(
                NotificationPriority.LOW,
                "Liquidity Alert Resolved",
                "%s wallet liquidity restored. Alert resolved by admin %s"
                        .formatted(alert.getCurrency(), event.resolvedByAdminId())
        );

        auditLogService.log(
                AuditAction.LIQUIDITY_ALERT_RESOLVED,
                event.resolvedByAdminId(),
                "Alert %s resolved for %s wallet".formatted(alert.getId(), alert.getCurrency())
        );
    }
}
