package com.epay.notification.listener;

import com.epay.common.config.messaging.RabbitMQConfig;
import com.epay.domain.notification.input.BlockUserWallet;
import com.epay.domain.notification.input.CreditWalletNotification;
import com.epay.domain.notification.input.DebitWalletNotification;
import com.epay.domain.notification.input.DepositWalletNotification;
import com.epay.domain.notification.input.MaintenanceDebtNotification;
import com.epay.domain.notification.input.MaintenanceDeductionNotification;
import com.epay.domain.notification.input.StatementPayload;
import com.epay.domain.notification.input.SwapCurrencyPayload;
import com.epay.domain.notification.input.WalletPinNotification;
import com.epay.notification.service.WalletNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletNotificationListener {

    private final WalletNotificationService walletNotificationService;

    @RabbitListener(queues = RabbitMQConfig.CREDIT_WALLET_QUEUE)
    public void onCreditWallet(CreditWalletNotification payload) {
        if (payload == null) { log.warn("[WalletListener] Null payload on credit queue"); return; }
        walletNotificationService.createCreditNotification(
                payload.getRecipientEmail(), payload.getTransferAmount(),
                payload.getSenderFullName(), payload.getReceiverFullName(),
                payload.getRecipientTotalBalance(), payload.getCurrency(),
                payload.getTransactionId(), payload.getPreviousBalance());
    }

    @RabbitListener(queues = RabbitMQConfig.DEBIT_WALLET_QUEUE)
    public void onDebitWallet(DebitWalletNotification payload) {
        if (payload == null) { log.warn("[WalletListener] Null payload on debit queue"); return; }
        walletNotificationService.createDebitNotification(
                payload.getSenderEmail(), payload.getFeeAmount(),
                payload.getTransferAmount(), payload.getSenderFullName(),
                payload.getReceiverFullName(), payload.getBalance(),
                payload.getCurrency(), payload.getTransactionId(),
                payload.getPreviousBalance());
    }

    @RabbitListener(queues = RabbitMQConfig.DEPOSIT_WALLET_QUEUE)
    public void onDepositWallet(DepositWalletNotification payload) {
        if (payload == null) { log.warn("[WalletListener] Null payload on deposit queue"); return; }
        walletNotificationService.createDepositNotification(
                payload.getRecipientEmail(), payload.getRecipientName(),
                payload.getDepositAmount(), payload.getAmount(),
                payload.getAccountHolder(), payload.getAvailableBalance(),
                payload.getPreviousBalance(), payload.getTerminalNumber(),
                payload.getCurrencySymbol());
    }

    @RabbitListener(queues = RabbitMQConfig.MAINTENANCE_DEDUCTION_QUEUE)
    public void onMaintenanceDeduction(MaintenanceDeductionNotification payload) {
        if (payload == null) { log.warn("[WalletListener] Null payload on maintenance queue"); return; }
        walletNotificationService.createMaintenanceNotification(payload);
    }

    @RabbitListener(queues = RabbitMQConfig.MAINTENANCE_DEBT_QUEUE)
    public void onMaintenanceDebt(MaintenanceDebtNotification payload) {
        if (payload == null) { log.warn("[WalletListener] Null payload on maintenance-debt queue"); return; }
        walletNotificationService.createMaintenanceDebtNotification(payload);
    }

    @RabbitListener(queues = RabbitMQConfig.SWAP_WALLET_QUEUE)
    public void onSwapWallet(SwapCurrencyPayload payload) {
        if (payload == null) { log.warn("[WalletListener] Null payload on swap queue"); return; }
        walletNotificationService.createSwapNotification(payload);
    }

    @RabbitListener(queues = RabbitMQConfig.BLOCK_USER_WALLET_QUEUE)
    public void onBlockUserWallet(BlockUserWallet payload) {
        if (payload == null) { log.warn("[WalletListener] Null payload on block-wallet queue"); return; }
        walletNotificationService.createBlockUserWalletNotification(
                payload.getEmail(), payload.getFirstName(),
                payload.getLastName(), payload.getMessage());
    }

    @RabbitListener(queues = RabbitMQConfig.WALLET_PIN_ALERT_QUEUE)
    public void onWalletPinAlert(WalletPinNotification payload) {
        if (payload == null) { log.warn("[WalletListener] Null payload on pin-alert queue"); return; }
        walletNotificationService.createWalletPinAlert(payload);
    }

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_STATEMENT_QUEUE)
    public void onAccountStatement(StatementPayload payload) {
        if (payload == null) { log.warn("[WalletListener] Null payload on statement queue"); return; }
        walletNotificationService.sendAccountStatement(payload);
    }
}
