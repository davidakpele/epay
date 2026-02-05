package pesco.notification_service.messageProducer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import pesco.notification_service.configurations.RabbitMQConfig;
import pesco.notification_service.payloads.BlockUserWallet;
import pesco.notification_service.payloads.CreditWalletNotification;
import pesco.notification_service.payloads.DebitWalletNotification;
import pesco.notification_service.payloads.DepositWalletNotification;
import pesco.notification_service.payloads.MaintenanceDeductionNotification;
import pesco.notification_service.payloads.StatementPayload;
import pesco.notification_service.payloads.SwapCurrencyPayload;

@Service
public class WalletMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public WalletMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Sends a notification for crediting a wallet.
     * 
     * @param recipientEmail
     * @param transferAmount
     * @param senderFullName
     * @param receiverFullName
     * @param RecipientTotalBalance
     */
    public void sendCreditWalletNotification(String recipientEmail, BigDecimal transferAmount, String senderFullName, String receiverFullName, BigDecimal RecipientTotalBalance, String currency, String transactionId, BigDecimal previousBalance) {
        CreditWalletNotification creditWalletNotification = new CreditWalletNotification(recipientEmail, transferAmount, senderFullName, receiverFullName, RecipientTotalBalance, currency, transactionId, previousBalance);
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_CREDIT_WALLET, creditWalletNotification);
    }

    /**
     * Sends a notification for debiting a wallet.
     * 
     * @param senderEmail
     * @param feeAmount
     * @param transferAmount
     * @param senderFullName
     * @param receiverFullName
     * @param balance
     */
    public void sendDebitWalletNotification(String senderEmail, BigDecimal feeAmount, BigDecimal transferAmount, String senderFullName, String receiverFullName, BigDecimal balance, String currency, String transactionId, BigDecimal previousBalance) {
        DebitWalletNotification debitWalletNotification = new DebitWalletNotification(senderEmail, feeAmount, transferAmount, senderFullName, receiverFullName, balance, currency, transactionId, previousBalance);
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_DEBIT_WALLET, debitWalletNotification);
    }

    /**
     * Sends a notification for depositing into a wallet.
     * 
     * @param recipientEmail
     * @param recipientName
     * @param depositAmount
     * @param transactionTime
     * @param totalBalance
     */
    public void sendDepositWalletNotification(
        String recipientEmail,
        String recipientName
        ,BigDecimal depositAmount,
        BigDecimal amount,
        String accountHolder,
        BigDecimal availableBalance,
        BigDecimal previousBalance,
        String terminalNumber,
        String currencySymbol) {
        DepositWalletNotification depositWalletNotification = new DepositWalletNotification(
            recipientEmail,
            recipientName,
            depositAmount,
            amount,
            previousBalance,
            availableBalance,
            terminalNumber,
            accountHolder,
            currencySymbol);
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_DEPOSIT_WALLET, depositWalletNotification);
    }

    /**
     * Sends a notification for maintainance to users.
     * 
     * @param email
     * @param firstName
     * @param amount
     * @param balance
     * @param content
     */
    public void sendMaintenanceNotification(
            String actionType,
            BigDecimal availableBalance,
            String currency,
            BigDecimal feeAmount,
            BigDecimal previousBalance,
            String reason,
            Boolean success,
            OffsetDateTime timestamp,
            BigDecimal totalAmountSpent,
            String userEmail,
            String userFirstName,
            Long userId,
            String userLastName
    ) {
        // Create a new notification payload using the updated constructor
        MaintenanceDeductionNotification maintenanceDeductionNotification =
                new MaintenanceDeductionNotification(
                        actionType,
                        availableBalance,
                        currency,
                        feeAmount,
                        previousBalance,
                        reason,
                        success,
                        timestamp,
                        totalAmountSpent,
                        userEmail,
                        userFirstName,
                        userId,
                        userLastName
                );

        // Send the notification via RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WALLET_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_WALLET_MAINTENANCE_SERVICE_DEDUCTION,
                maintenanceDeductionNotification
        );
    }


    /**
     * 
     * @param email
     * @param period
     * @param username
     * @param pdfBytes
     */
    public void sendAccountStatement(String email, String period, String username, byte[] pdfBytes) {
        StatementPayload statement_of_account = new StatementPayload(email, username, pdfBytes, period);
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACCOUNT_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_ACCOUNT_STATEMENT,
                statement_of_account
            );
        } catch (AmqpException e) {
            System.out.println("[ERROR] Failed to send account statement to RabbitMQ");
            System.out.println("Error Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

     /**
     * 
     * @param email
     * @param period
     * @param username
     * @param pdfBytes
     */
    public void sendSwapNotification(String email, BigDecimal amount, String name,  BigDecimal availableBalance, 
        BigDecimal previousBalance, String currency, String currencyExchange) {
        SwapCurrencyPayload request = new SwapCurrencyPayload();
        request.setAmount(amount);
        request.setEmail(email);
        request.setAccountHolder(name);
        request.setPreviousBalance(previousBalance);
        request.setAvailableBalance(availableBalance);
        request.setCurrencySymbol(currency);
        request.setCurrencyExchange(currencyExchange);
           
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_SWAP_WALLET, request);
        } catch (AmqpException e) {
            throw e;
        }
    }

     /**
     * 
     * @param email
     * @param firstName
     * @param lastName
     * @param message
     */
    public void sendBlockUserWalletMessage(String email, String firstName,  String lastName, String message) {
        BlockUserWallet request = new BlockUserWallet();

        request.setEmail(email);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setMessage(message);
           
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EXCHANGE, RabbitMQConfig.ROUTING_KEY_BLOCK_USER_WALLET, request);
        } catch (AmqpException e) {
            throw e;
        }
    }   
}
