package com.epay.common.interfaces;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Publisher interface for wallet-related notifications.
 * Defined in common — implemented in notification via RabbitMQ.
 * Inject this in wallet module, not WalletNotificationMessageProducer directly.
 */
public interface IWalletNotificationPublisher {

    void publishCreditNotification(String recipientEmail, BigDecimal transferAmount,
                                   String senderFullName, String receiverFullName,
                                   BigDecimal recipientTotalBalance, String currency,
                                   String transactionId, BigDecimal previousBalance);

    void publishDebitNotification(String senderEmail, BigDecimal feeAmount,
                                  BigDecimal transferAmount, String senderFullName,
                                  String receiverFullName, BigDecimal balance,
                                  String currency, String transactionId,
                                  BigDecimal previousBalance);

    void publishDepositNotification(String recipientEmail, String recipientName,
                                    BigDecimal depositAmount, BigDecimal amount,
                                    String accountHolder, BigDecimal availableBalance,
                                    BigDecimal previousBalance, String terminalNumber,
                                    String currencySymbol);

    void publishMaintenanceNotification(String actionType, BigDecimal availableBalance,
                                        String currency, BigDecimal feeAmount,
                                        BigDecimal previousBalance, String reason,
                                        Boolean success, OffsetDateTime timestamp,
                                        BigDecimal totalAmountSpent, String userEmail,
                                        String userFirstName, Long userId, String userLastName);

    void publishSwapNotification(String email, BigDecimal amount, String name,
                                 BigDecimal availableBalance, BigDecimal previousBalance,
                                 String currency, String currencyExchange);

    void publishBlockUserWallet(String email, String firstName, String lastName, String message);

    void publishWalletPinAlert(String email, String username, String eventType,
                               String eventTime, String ipAddress, String deviceInfo);
}
