package com.epay.notification.publisher;

import com.epay.common.config.messaging.RabbitMQConfig;
import com.epay.common.interfaces.IWalletNotificationPublisher;
import com.epay.domain.notification.input.BlockUserWallet;
import com.epay.domain.notification.input.CreditWalletNotification;
import com.epay.domain.notification.input.DebitWalletNotification;
import com.epay.domain.notification.input.DepositWalletNotification;
import com.epay.domain.notification.input.MaintenanceDeductionNotification;
import com.epay.domain.notification.input.StatementPayload;
import com.epay.domain.notification.input.SwapCurrencyPayload;
import com.epay.domain.notification.input.WalletPinNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletNotificationMessageProducer implements IWalletNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishCreditNotification(String recipientEmail, BigDecimal transferAmount,
                                          String senderFullName, String receiverFullName,
                                          BigDecimal recipientTotalBalance, String currency,
                                          String transactionId, BigDecimal previousBalance) {
        send(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_CREDIT_WALLET,
                new CreditWalletNotification(recipientEmail, transferAmount, senderFullName,
                        receiverFullName, recipientTotalBalance, currency, transactionId, previousBalance));
    }

    @Override
    public void publishDebitNotification(String senderEmail, BigDecimal feeAmount,
                                         BigDecimal transferAmount, String senderFullName,
                                         String receiverFullName, BigDecimal balance,
                                         String currency, String transactionId,
                                         BigDecimal previousBalance) {
        send(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_DEBIT_WALLET,
                new DebitWalletNotification(senderEmail, feeAmount, transferAmount,
                        senderFullName, receiverFullName, balance, currency, transactionId, previousBalance));
    }

    @Override
    public void publishDepositNotification(String recipientEmail, String recipientName,
                                           BigDecimal depositAmount, BigDecimal amount,
                                           String accountHolder, BigDecimal availableBalance,
                                           BigDecimal previousBalance, String terminalNumber,
                                           String currencySymbol) {
        send(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_DEPOSIT_WALLET,
                new DepositWalletNotification(recipientEmail, recipientName, depositAmount,
                        amount, previousBalance, availableBalance, terminalNumber, accountHolder, currencySymbol));
    }

    @Override
    public void publishMaintenanceNotification(String actionType, BigDecimal availableBalance,
                                               String currency, BigDecimal feeAmount,
                                               BigDecimal previousBalance, String reason,
                                               Boolean success, OffsetDateTime timestamp,
                                               BigDecimal totalAmountSpent, String userEmail,
                                               String userFirstName, Long userId, String userLastName) {
        send(RabbitMQConfig.WALLET_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_WALLET_MAINTENANCE_SERVICE_DEDUCTION,
                new MaintenanceDeductionNotification(actionType, availableBalance, currency,
                        feeAmount, previousBalance, reason, success, timestamp,
                        totalAmountSpent, userEmail, userFirstName, userId, userLastName));
    }

    @Override
    public void publishSwapNotification(String email, BigDecimal amount, String name,
                                        BigDecimal availableBalance, BigDecimal previousBalance,
                                        String currency, String currencyExchange) {
        SwapCurrencyPayload payload = new SwapCurrencyPayload();
        payload.setAmount(amount);
        payload.setEmail(email);
        payload.setAccountHolder(name);
        payload.setPreviousBalance(previousBalance);
        payload.setAvailableBalance(availableBalance);
        payload.setCurrencySymbol(currency);
        payload.setCurrencyExchange(currencyExchange);
        send(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_SWAP_WALLET, payload);
    }

    @Override
    public void publishBlockUserWallet(String email, String firstName, String lastName, String message) {
        BlockUserWallet payload = new BlockUserWallet();
        payload.setEmail(email);
        payload.setFirstName(firstName);
        payload.setLastName(lastName);
        payload.setMessage(message);
        send(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_BLOCK_USER_WALLET, payload);
    }

    @Override
    public void publishWalletPinAlert(String email, String username, String eventType,
                                      String eventTime, String ipAddress, String deviceInfo) {
        WalletPinNotification payload = new WalletPinNotification();
        payload.setEmail(email);
        payload.setUsername(username);
        payload.setAction(eventType);
        payload.setActionTime(eventTime);
        payload.setIpAddress(ipAddress);
        payload.setDeviceInfo(deviceInfo);
        send(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_WALLET_PIN_ALERT, payload);
    }

    public void sendAccountStatement(String email, String period, String username, byte[] pdfBytes) {
        send(RabbitMQConfig.ACCOUNT_EXCHANGE, RabbitMQConfig.ROUTING_KEY_ACCOUNT_STATEMENT,
                new StatementPayload(email, username, pdfBytes, period));
    }

    private void send(String exchange, String routingKey, Object payload) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
        } catch (AmqpException e) {
            log.error("[WalletNotification] Failed to publish {}/{}: {}", exchange, routingKey, e.getMessage());
            throw e;
        }
    }
}
