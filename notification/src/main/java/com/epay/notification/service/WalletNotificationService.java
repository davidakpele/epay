package com.epay.notification.service;

import com.epay.domain.notification.input.MaintenanceDeductionNotification;
import com.epay.domain.notification.input.StatementPayload;
import com.epay.domain.notification.input.SwapCurrencyPayload;
import com.epay.domain.notification.input.WalletPinNotification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletNotificationService {

    private final JavaMailSender        javaMailSender;
    private final SpringTemplateEngine  templateEngine;

    @Async
    public CompletableFuture<Void> createCreditNotification(
            String recipientEmail, BigDecimal transferAmount,
            String senderFullName, String receiverFullName,
            BigDecimal recipientTotalBalance, String currency,
            String transactionId, BigDecimal previousBalance) {

        Context ctx = new Context();
        ctx.setVariable("receiverFullName",       receiverFullName);
        ctx.setVariable("senderFullName",         senderFullName);
        ctx.setVariable("transferAmount",         transferAmount);
        ctx.setVariable("currency",               currency);
        ctx.setVariable("previousBalance",        previousBalance);
        ctx.setVariable("recipientTotalBalance",  recipientTotalBalance);
        ctx.setVariable("transactionId",          transactionId);
        return send(recipientEmail, "ePay — Funds Received", "wallet/credit-notification", ctx);
    }

    @Async
    public CompletableFuture<Void> createDebitNotification(
            String senderEmail, BigDecimal feeAmount,
            BigDecimal transferAmount, String senderFullName,
            String receiverFullName, BigDecimal balance,
            String currency, String transactionId,
            BigDecimal previousBalance) {

        Context ctx = new Context();
        ctx.setVariable("senderFullName",   senderFullName);
        ctx.setVariable("receiverFullName", receiverFullName);
        ctx.setVariable("transferAmount",   transferAmount);
        ctx.setVariable("feeAmount",        feeAmount);
        ctx.setVariable("currency",         currency);
        ctx.setVariable("balance",          balance);
        ctx.setVariable("previousBalance",  previousBalance);
        ctx.setVariable("transactionId",    transactionId);
        return send(senderEmail, "ePay — Transfer Sent", "wallet/debit-notification", ctx);
    }

    @Async
    public CompletableFuture<Void> createDepositNotification(
            String recipientEmail, String recipientName,
            BigDecimal depositAmount, BigDecimal amount,
            String accountHolder, BigDecimal availableBalance,
            BigDecimal previousBalance, String terminalNumber,
            String currencySymbol) {

        Context ctx = new Context();
        ctx.setVariable("recipientName",     recipientName);
        ctx.setVariable("accountHolder",     accountHolder);
        ctx.setVariable("depositAmount",     depositAmount);
        ctx.setVariable("amount",            amount);
        ctx.setVariable("availableBalance",  availableBalance);
        ctx.setVariable("previousBalance",   previousBalance);
        ctx.setVariable("terminalNumber",    terminalNumber);
        ctx.setVariable("currencySymbol",    currencySymbol);
        return send(recipientEmail, "ePay — Deposit Successful", "deposit/deposit-success", ctx);
    }

    @Async
    public CompletableFuture<Void> createMaintenanceNotification(MaintenanceDeductionNotification n) {
        Context ctx = new Context();
        ctx.setVariable("userFirstName",     n.getUserFirstName());
        ctx.setVariable("userLastName",      n.getUserLastName());
        ctx.setVariable("feeAmount",         n.getFeeAmount());
        ctx.setVariable("currency",          n.getCurrency());
        ctx.setVariable("availableBalance",  n.getAvailableBalance());
        ctx.setVariable("previousBalance",   n.getPreviousBalance());
        ctx.setVariable("reason",            n.getReason());
        ctx.setVariable("actionType",        n.getActionType());
        ctx.setVariable("timestamp",         n.getTimestamp());
        ctx.setVariable("success",           n.getSuccess());
        return send(n.getUserEmail(), "ePay — Maintenance Fee Deducted",
                "maintenance/maintenance-fee-deducted", ctx);
    }

    @Async
    public CompletableFuture<Void> createSwapNotification(SwapCurrencyPayload p) {
        Context ctx = new Context();
        ctx.setVariable("accountHolder",    p.getAccountHolder());
        ctx.setVariable("amount",           p.getAmount());
        ctx.setVariable("availableBalance", p.getAvailableBalance());
        ctx.setVariable("previousBalance",  p.getPreviousBalance());
        ctx.setVariable("currencySymbol",   p.getCurrencySymbol());
        ctx.setVariable("currencyExchange", p.getCurrencyExchange());
        return send(p.getEmail(), "ePay — Currency Swap Successful", "wallet/swap-notification", ctx);
    }

    @Async
    public CompletableFuture<Void> createBlockUserWalletNotification(
            String email, String firstName, String lastName, String message) {

        Context ctx = new Context();
        ctx.setVariable("firstName", firstName);
        ctx.setVariable("lastName",  lastName);
        ctx.setVariable("message",   message);
        return send(email, "ePay — Account Action Required", "wallet/debit-notification", ctx);
    }

    @Async
    public CompletableFuture<Void> createWalletPinAlert(WalletPinNotification p) {
        Context ctx = new Context();
        ctx.setVariable("fullName",     p.getFullName());
        ctx.setVariable("username",     p.getUsername());
        ctx.setVariable("action",       p.getAction());
        ctx.setVariable("actionTime",   p.getActionTime());
        ctx.setVariable("ipAddress",    p.getIpAddress());
        ctx.setVariable("deviceInfo",   p.getDeviceInfo());
        ctx.setVariable("supportPhone", p.getSupportPhone());
        ctx.setVariable("supportEmail", p.getSupportEmail());
        return send(p.getEmail(), "ePay — Wallet PIN " + p.getAction(),
                "auth/account-security-alert", ctx);
    }

    @Async
    public CompletableFuture<Void> sendAccountStatement(StatementPayload p) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            Context ctx = new Context();
            ctx.setVariable("username", p.getUsername());
            ctx.setVariable("period",   p.getPeriod());
            String html = templateEngine.process("auth/account-statement", ctx);
            helper.setTo(p.getEmail());
            helper.setSubject("ePay — Your Account Statement");
            helper.setText(html, true);

            if (p.getPdfBytes() != null && p.getPdfBytes().length > 0) {
                String filename = "account-statement-"
                        + (p.getPeriod() != null ? p.getPeriod().replace(" ", "-") : "period")
                        + ".pdf";
                helper.addAttachment(filename,
                        new org.springframework.core.io.ByteArrayResource(p.getPdfBytes()),
                        "application/pdf");
            }

            javaMailSender.send(mimeMessage);
            log.info("[Statement] Sent to {}", p.getEmail());
            return CompletableFuture.completedFuture(null);
        } catch (jakarta.mail.MessagingException | MailException e) {
            log.error("Failed to send statement to {}: {}", p.getEmail(), e.getMessage());
            throw new MailSendException("Failed to send statement email: " + e.getMessage(), e);
        }
    }

    private CompletableFuture<Void> send(String to, String subject, String template, Context ctx) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(templateEngine.process(template, ctx), true);
            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to {} (template={}): {}", to, template, e.getMessage());
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
