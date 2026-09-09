package com.epay.common.interfaces;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


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

    /**
     * Notifies a user that a maintenance fee could not be fully deducted because the
     * wallet balance was insufficient, and a debt has been created/increased.
     *
     * @param userEmail        recipient email address
     * @param userFirstName    recipient first name (for greeting)
     * @param userLastName     recipient last name
     * @param userId           internal user ID
     * @param currency         ISO-4217 currency code of the debt (e.g. "USD")
     * @param feeAmount        the total fee that was due
     * @param deductedAmount   how much was actually deducted (may be zero for full debt)
     * @param debtAmount       how much has been recorded as debt
     * @param walletBalance    current wallet balance after deduction attempt
     * @param billingMonth     human-readable billing period, e.g. "September 2026"
     */
    void publishDebtCreatedNotification(String userEmail, String userFirstName, String userLastName,
                                        Long userId, String currency, BigDecimal feeAmount,
                                        BigDecimal deductedAmount, BigDecimal debtAmount,
                                        BigDecimal walletBalance, String billingMonth);

    /**
     * Notifies a user that an outstanding maintenance fee debt has been fully or
     * partially repaid from a recent deposit/credit.
     *
     * @param userEmail       recipient email address
     * @param userFirstName   recipient first name
     * @param userLastName    recipient last name
     * @param userId          internal user ID
     * @param currency        currency of the debt that was settled
     * @param repaidAmount    amount that was recovered in this event
     * @param remainingDebt   remaining debt after this repayment (zero = fully settled)
     * @param newWalletBalance wallet balance after debt deduction
     * @param fullySettled    true when the entire debt has been cleared
     */
    void publishDebtRepaidNotification(String userEmail, String userFirstName, String userLastName,
                                       Long userId, String currency, BigDecimal repaidAmount,
                                       BigDecimal remainingDebt, BigDecimal newWalletBalance,
                                       boolean fullySettled);
}
