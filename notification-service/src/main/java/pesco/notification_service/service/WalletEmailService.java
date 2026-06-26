package pesco.notification_service.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import pesco.notification_service.configurations.RabbitMQConfig;
import pesco.notification_service.enums.Symbols;
import pesco.notification_service.payloads.BlockUserWallet;
import pesco.notification_service.payloads.CreditWalletNotification;
import pesco.notification_service.payloads.DebitWalletNotification;
import pesco.notification_service.payloads.DepositWalletNotification;
import pesco.notification_service.payloads.MaintenanceDeductionNotification;
import pesco.notification_service.payloads.StatementPayload;
import pesco.notification_service.payloads.SwapCurrencyPayload;
import pesco.notification_service.utils.KeyHelper;

@Service
public class WalletEmailService {
    
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final SecureRandom random = new SecureRandom();
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");


    public WalletEmailService(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    public String generateUniqueId() {
        long timestamp = System.currentTimeMillis();
        Random random = new Random();
        long randomNum = random.nextLong(1000000000000000L);
        return String.format("%014d%016d", timestamp, randomNum);
    }


    @RabbitListener(queues = RabbitMQConfig.CREDIT_WALLET_QUEUE)
    public void receiveCreditWalletNotificationEmail(CreditWalletNotification creditWalletNotification) {
        if (creditWalletNotification != null) {
            sendCreditWalletNotificationToRecipient(
                    creditWalletNotification.getRecipientEmail(),
                    creditWalletNotification.getTransferAmount(),
                    creditWalletNotification.getSenderFullName(),
                    creditWalletNotification.getReceiverFullName(),
                    creditWalletNotification.getRecipientTotalBalance(),
                    creditWalletNotification.getCurrency(),
                    creditWalletNotification.getTransactionId(), 
                    creditWalletNotification.getPreviousBalance());
        } else {
            System.out.println("Failed to deserialize email request.");
        }
    }

    @Async
    public CompletableFuture<Void> sendCreditWalletNotificationToRecipient(String recipientEmail,
            BigDecimal transferAmount, String senderFullName, String receiverFullName,
            BigDecimal recipientTotalBalance, String currency, String transactionId, BigDecimal previousBalance) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            Symbols currencySymbols = Symbols.valueOf(currency.toUpperCase());
            String symbol = currencySymbols.getSymbol();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
            Context context = new Context();
            context.setVariable("recipientName", receiverFullName);
            context.setVariable("senderName", senderFullName);
            context.setVariable("transactionId", transactionId);
            context.setVariable("type", "CREDITED");
            context.setVariable("amount", symbol + KeyHelper.FormatBigDecimal(transferAmount));
            context.setVariable("description", "Credited " + symbol + KeyHelper.FormatBigDecimal(transferAmount) + " by " + senderFullName);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = LocalDateTime.now().format(formatter);
            context.setVariable("time", formattedTime);
            context.setVariable("previousBalance", symbol + KeyHelper.FormatBigDecimal(previousBalance));
            context.setVariable("totalBalance", symbol + KeyHelper.FormatBigDecimal(recipientTotalBalance));

            String htmlContent = templateEngine.process("TransactionNotification", context);

            mimeMessageHelper.setTo(recipientEmail);
            mimeMessageHelper.setSubject("Credit Notification - " + symbol + KeyHelper.FormatBigDecimal(transferAmount) + " Received");
            mimeMessageHelper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = RabbitMQConfig.DEBIT_WALLET_QUEUE)
    public void receiveDebitWalletNotificationEmail(DebitWalletNotification debitWalletNotification) {
        if (debitWalletNotification != null) {
            sendDebitWalletNotificationToRecipient(
                    debitWalletNotification.getSenderEmail(),
                    debitWalletNotification.getFeeAmount(),
                    debitWalletNotification.getTransferAmount(),
                    debitWalletNotification.getSenderFullName(),
                    debitWalletNotification.getReceiverFullName(),
                    debitWalletNotification.getBalance(),
                    debitWalletNotification.getCurrency(),
                    debitWalletNotification.getTransactionId(),
                    debitWalletNotification.getPreviousBalance());
        }
    }

    @Async
    public CompletableFuture<Void> sendDebitWalletNotificationToRecipient(String senderEmail,
            BigDecimal feeAmount, BigDecimal transferAmount, String senderFullName, 
            String receiverFullName, BigDecimal balance, String currency, String transactionId, BigDecimal previousBalance) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            Symbols currencySymbols = Symbols.valueOf(currency.toUpperCase());
            String symbol = currencySymbols.getSymbol();
            // Create MimeMessageHelper
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            // Prepare the HTML template context for debit
            Context context = new Context();
            context.setVariable("recipientName", senderFullName);
            context.setVariable("senderName", receiverFullName);
            context.setVariable("transactionId", transactionId);
            context.setVariable("type", "DEBITED");
            context.setVariable("amount", symbol + KeyHelper.FormatBigDecimal(transferAmount.add(feeAmount)));
            context.setVariable("description", "Sent " + symbol + KeyHelper.FormatBigDecimal(transferAmount) + 
                    " to " + receiverFullName + " (Fee: " + symbol + KeyHelper.FormatBigDecimal(feeAmount) + ")");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = LocalDateTime.now().format(formatter);
            context.setVariable("time", formattedTime);
            context.setVariable("totalBalance", symbol + KeyHelper.FormatBigDecimal(balance));
            context.setVariable("previousBalance", symbol + KeyHelper.FormatBigDecimal(previousBalance));

            String htmlContent = templateEngine.process("TransactionNotification", context);

            // Set email attributes
            mimeMessageHelper.setTo(senderEmail);
            mimeMessageHelper.setSubject("Debit Notification - " + symbol + KeyHelper.FormatBigDecimal(transferAmount) + " Sent");
            mimeMessageHelper.setText(htmlContent, true);

            // Send the email
            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @RabbitListener(queues =RabbitMQConfig.DEPOSIT_WALLET_QUEUE)
    public void receiveDepositWalletNotificationEmail(DepositWalletNotification depositWalletNotification) {
        if (depositWalletNotification != null) {
            sendDepositNotification(
                    depositWalletNotification.getRecipientEmail(),
                    depositWalletNotification.getRecipientName(),
                    depositWalletNotification.getDepositAmount(),
                    depositWalletNotification.getAmount(),
                    depositWalletNotification.getAccountHolder(),
                    depositWalletNotification.getAvailableBalance(),
                    depositWalletNotification.getPreviousBalance(),
                    depositWalletNotification.getTerminalNumber(),
                    depositWalletNotification.getCurrencySymbol());
        } else {
            System.out.println("Failed to deserialize email request.");
        }
    }

    @Async
    public CompletableFuture<Void> sendDepositNotification(
        String recipientEmail, 
        String recipientName,
        BigDecimal depositAmount, 
        BigDecimal totalBalance, 
        String accountHolder, 
        BigDecimal availableBalance, 
        BigDecimal previousBalance, 
        String terminalNumber,
        String currencySymbol) { 
        
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
            Context context = new Context();
            String receiptId = generateReceiptId();
            String transactionId = "TXN-" + receiptId.substring(4); 
            LocalDateTime now = LocalDateTime.now();
            context.setVariable("username", recipientName);
            context.setVariable("accountHolder", accountHolder);
            context.setVariable("terminalNumber", terminalNumber);
            context.setVariable("currencySymbol", currencySymbol);
            context.setVariable("amount", KeyHelper.FormatBigDecimal(depositAmount));
            context.setVariable("previousBalance", KeyHelper.FormatBigDecimal(previousBalance));
            context.setVariable("totalBalance", KeyHelper.FormatBigDecimal(totalBalance));
            context.setVariable("availableBalance", KeyHelper.FormatBigDecimal(availableBalance));

            context.setVariable("transactionId", transactionId);
            context.setVariable("receiptNo", transactionId);
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            context.setVariable("transactionDate", now.format(dateFormatter));
            context.setVariable("transactionTime", now.format(timeFormatter));
            
            context.setVariable("amountInWords", convertAmountToWords(depositAmount, currencySymbol));

            String htmlContent = templateEngine.process("DepositNotification", context);

            mimeMessageHelper.setTo(recipientEmail);
            mimeMessageHelper.setSubject("Deposit Successful - Receipt #" + context.getVariable("receiptNo"));
            mimeMessageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.MAINTENANCE_DEDUCTION_QUEUE)
    public void receiveMaintenanceDeductionNotificationEmail(
            MaintenanceDeductionNotification maintenanceDeductionNotification) {

        if (maintenanceDeductionNotification != null) {
            sendMaintenanceDeductionNotification(
                    maintenanceDeductionNotification.getUserEmail(),
                    maintenanceDeductionNotification.getUserFirstName(),
                    maintenanceDeductionNotification.getUserLastName(),
                    maintenanceDeductionNotification.getActionType(),
                    maintenanceDeductionNotification.getFeeAmount(),
                    maintenanceDeductionNotification.getPreviousBalance(),
                    maintenanceDeductionNotification.getAvailableBalance(),
                    maintenanceDeductionNotification.getCurrency(),
                    maintenanceDeductionNotification.getReason(),
                    maintenanceDeductionNotification.getTimestamp(),
                    maintenanceDeductionNotification.getSuccess(),
                    maintenanceDeductionNotification.getTotalAmountSpent()
            );
        } else {
            System.out.println("❌ Failed to deserialize maintenance deduction email request.");
        }
    }

    @Async
    public CompletableFuture<Void> sendMaintenanceDeductionNotification(
            String userEmail,
            String userFirstName,
            String userLastName,
            String actionType,
            BigDecimal feeAmount,
            BigDecimal previousBalance,
            BigDecimal availableBalance,
            String currency,
            String reason,
            OffsetDateTime timestamp,
            Boolean success,
            BigDecimal totalAmountSpent
    ) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

            Context context = new Context();
            String fullName = userFirstName + " " + userLastName;
            context.setVariable("username", fullName);
            context.setVariable("feeAmount", KeyHelper.FormatBigDecimal(feeAmount) + " " + currency);
            context.setVariable("currency", currency);
            context.setVariable("reason", "Maintenance Fee");
            context.setVariable("previousBalance", KeyHelper.FormatBigDecimal(previousBalance));
            context.setVariable("availableBalance", KeyHelper.FormatBigDecimal(availableBalance));
            context.setVariable("totalAmountSpent", KeyHelper.FormatBigDecimal(totalAmountSpent));
            context.setVariable("actionType", actionType);
            context.setVariable("status", "Successful");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a");
            String formattedTime = timestamp != null
                    ? timestamp.toLocalDateTime().format(formatter)
                    : LocalDateTime.now().format(formatter);
            context.setVariable("transactionTime", formattedTime);

            String htmlContent = templateEngine.process("MaintenanceDeductionNotification", context);

            mimeMessageHelper.setTo(userEmail);
            mimeMessageHelper.setSubject(
                    success
                            ? "Maintenance Fee Deduction Successful"
                            : "Maintenance Fee Deduction Failed"
            );
            mimeMessageHelper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send maintenance email: " + e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_STATEMENT_QUEUE)
    public void receiveAccountStatement(StatementPayload request) {
        if (request != null && request.getEmail() != null) {
            try {
                sendAccountStatementPDF(request.getEmail(), request.getPdfBytes(), request.getUsername(), request.getPeriod());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid request received in RabbitMQ listener");
        }
    }

    @Async
    public CompletableFuture<Void> sendAccountStatementPDF(
        String email, 
        byte[] pdfAttachment, 
        String username, 
        String period) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("period", period);
            context.setVariable("bankName", "Payrix");
            context.setVariable("bankAddress", "123 Financial District, Cityville");
            context.setVariable("bankPhone", "(555) 123-4567");
            context.setVariable("bankEmail", "support@payrix.com");
            String htmlContent = templateEngine.process("AccountStatement", context);
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Your Payrix Account Statement - " + period);
            mimeMessageHelper.setText(htmlContent, true);
            mimeMessageHelper.addAttachment("bank-statement.pdf", 
                new ByteArrayResource(pdfAttachment), 
                "application/pdf");
            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture(null);

        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SWAP_WALLET_QUEUE)
    public void receiveSwapNotificationEmail(SwapCurrencyPayload request) {
        if (request != null) {
            sendSwapAlert(request);
        } else {
            System.out.println("Failed to deserialize email request.");
        }
    }

    @Async
    public CompletableFuture<Void> sendSwapAlert(SwapCurrencyPayload request) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
            Context context = new Context();
            context.setVariable("recipientName", request.getAccountHolder());
            context.setVariable("amount", request.getCurrencySymbol() + KeyHelper.FormatBigDecimal(request.getAmount()));
            context.setVariable("previousBalance", request.getCurrencySymbol() + KeyHelper.FormatBigDecimal(request.getPreviousBalance()));
            context.setVariable("availableBalance", request.getCurrencySymbol() + KeyHelper.FormatBigDecimal(request.getAvailableBalance()));
            context.setVariable("currencyPair", request.getCurrencyExchange());
            context.setVariable("transactionId", "SWP-" + System.currentTimeMillis());
            context.setVariable("description", "Currency exchange transaction completed");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            context.setVariable("time", LocalDateTime.now().format(formatter));
            String htmlContent = templateEngine.process("SwapNotification", context);

            mimeMessageHelper.setTo(request.getEmail());
            mimeMessageHelper.setSubject("Currency Swap Completed - " + 
                request.getCurrencySymbol() + KeyHelper.FormatBigDecimal(request.getAmount()));
            mimeMessageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

            return CompletableFuture.completedFuture(null);

        } catch (MessagingException | MailException e) {

            e.printStackTrace();
            throw new MailSendException("Failed to send swap email: " + e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.BLOCK_USER_WALLET_QUEUE)
    public void receiveBlockUserWalletMessage(BlockUserWallet request) {
        if (request != null) {
            sendUserBlockWalletMessage(request);
        } else {
            System.out.println("Failed to deserialize email request.");
        }
    } 

    @Async
    public CompletableFuture<Void> sendUserBlockWalletMessage(BlockUserWallet request) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Format names
            String firstName = capitalize(request.getFirstName());
            String lastName = capitalize(request.getLastName());
            String fullName = firstName + " " + lastName;

            // Template variables
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("lastName", lastName);
            context.setVariable("fullName", fullName); 
            context.setVariable("message", request.getMessage());
            String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            context.setVariable("currentDate", formattedDate);

            String referenceId = generateBlockId();
            context.setVariable("referenceId", referenceId);
            String htmlContent = templateEngine.process("BlockUserWalletNotification", context);

            mimeMessageHelper.setTo(request.getEmail());
            mimeMessageHelper.setSubject("Wallet Blocked Notification");
            mimeMessageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

            return CompletableFuture.completedFuture(null);

        } catch (MessagingException | MailException e) {

            e.printStackTrace();
            throw new MailSendException("Failed to send block wallet email: " + e.getMessage(), e);
        }
    }


    private String convertAmountToWords(BigDecimal amount, String currencySymbol) {
        if (amount == null) return "Zero Only";

        Map<String, String[]> currencyNames = new HashMap<>();
        currencyNames.put("USD", new String[]{"Dollar", "Cent"});
        currencyNames.put("EUR", new String[]{"Euro", "Cent"});
        currencyNames.put("NGN", new String[]{"Naira", "Kobo"});
        currencyNames.put("GBP", new String[]{"Pound", "Pence"});
        currencyNames.put("JPY", new String[]{"Yen", ""}); 
        currencyNames.put("AUD", new String[]{"Australian Dollar", "Cent"});
        currencyNames.put("CAD", new String[]{"Canadian Dollar", "Cent"});
        currencyNames.put("CHF", new String[]{"Swiss Franc", "Rappen"});
        currencyNames.put("CNY", new String[]{"Yuan", "Fen"});
        currencyNames.put("INR", new String[]{"Rupee", "Paisa"});

        // Map symbol to currency code
        String currencyCode = Symbols.getCurrencyCodeBySymbol(currencySymbol);
        String[] names = currencyNames.getOrDefault(currencyCode, new String[]{"Dollar", "Cent"});
        String major = names[0];
        String minor = names[1];

        boolean isNegative = amount.compareTo(BigDecimal.ZERO) < 0;
        if (isNegative) amount = amount.abs();

        long wholePart = amount.longValue();
        int fractionPart = amount.remainder(BigDecimal.ONE).multiply(new BigDecimal(100)).intValue();

        String wholeInWords = convertNumberToWords(wholePart);
        String fractionInWords = convertNumberToWords(fractionPart);

        StringBuilder result = new StringBuilder();

        if (isNegative) result.append("Minus ");

        if (wholePart > 0) {
            result.append(wholeInWords).append(" ").append(major);
            if (wholePart > 1) result.append("s");
        } else {
            result.append("Zero ").append(major);
        }

        if (fractionPart > 0 && !minor.isEmpty()) {
            result.append(" and ").append(fractionInWords).append(" ").append(minor);
            if (fractionPart > 1) result.append("s");
        }

        result.append(" Only");
        return result.toString();
    }

    private String convertNumberToWords(long number) {
        if (number == 0) {
            return "Zero";
        }

        if (number < 0) {
            return "Minus " + convertNumberToWords(-number);
        }

        String words = "";

        if ((number / 1_000_000_000) > 0) {
            words += convertNumberToWords(number / 1_000_000_000) + " Billion ";
            number %= 1_000_000_000;
        }

        if ((number / 1_000_000) > 0) {
            words += convertNumberToWords(number / 1_000_000) + " Million ";
            number %= 1_000_000;
        }

        if ((number / 1_000) > 0) {
            words += convertNumberToWords(number / 1_000) + " Thousand ";
            number %= 1_000;
        }

        if ((number / 100) > 0) {
            words += convertNumberToWords(number / 100) + " Hundred ";
            number %= 100;
        }

        if (number > 0) {
            if (!words.isEmpty()) {
                words += "and ";
            }

            String[] unitsArray = {
                "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven",
                "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen",
                "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
            };

            String[] tensArray = {
                "Zero", "Ten", "Twenty", "Thirty", "Forty", "Fifty", "Sixty",
                "Seventy", "Eighty", "Ninety"
            };

            if (number < 20) {
                words += unitsArray[(int) number];
            } else {
                words += tensArray[(int) (number / 10)];
                if ((number % 10) > 0) {
                    words += "-" + unitsArray[(int) (number % 10)];
                }
            }
        }

        return words.trim();
    }

    private String generateReceiptId() {
        StringBuilder receiptId = new StringBuilder("RCT-");
        
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(CHARACTERS.length());
            receiptId.append(CHARACTERS.charAt(index));
        }
        
        return receiptId.toString();
    }

    private String capitalize(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        name = name.trim().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static synchronized String generateBlockId() {
        String date = LocalDate.now().format(DATE_FORMAT);
        String sequence = String.format("%03d", counter.getAndIncrement());

        return "BLK-" + date + "-" + sequence;
    }


}
