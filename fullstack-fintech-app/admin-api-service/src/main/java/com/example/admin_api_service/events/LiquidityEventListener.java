package com.example.admin_api_service.events;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.admin_api_service.components.LiquidityAlertResolvedEvent;
import com.example.admin_api_service.components.LiquidityAlertTriggeredEvent;
import com.example.admin_api_service.components.WalletFundedEvent;
import com.example.admin_api_service.components.WalletRebalancedEvent;
import com.example.admin_api_service.components.WalletWithdrawnEvent;
import com.example.admin_api_service.models.LiquidityThresholdAlert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class LiquidityEventListener {

    private final NotificationService notificationService;   // your existing notification service
    private final AuditLogService auditLogService;           // your existing audit service

    @EventListener
    @Async  // non-blocking — won't slow down the transaction that published it
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
