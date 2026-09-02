package com.epay.notification.service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.epay.common.util.MoneyFormatter;
import com.epay.domain.notification.input.MaintenanceDeductionNotification;
import com.epay.domain.notification.input.StatementPayload;
import com.epay.domain.notification.input.SwapCurrencyPayload;
import com.epay.domain.notification.input.WalletPinNotification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        // currency here is a code (e.g. "NGN"), not a symbol — format as "NGN 1,000.00"
        Context ctx = new Context();
        ctx.setVariable("receiverFullName",       receiverFullName);
        ctx.setVariable("senderFullName",         senderFullName);
        ctx.setVariable("transferAmount",         MoneyFormatter.formatWithCode(currency, transferAmount));
        ctx.setVariable("previousBalance",        MoneyFormatter.formatWithCode(currency, previousBalance));
        ctx.setVariable("recipientTotalBalance",  MoneyFormatter.formatWithCode(currency, recipientTotalBalance));
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

        // currency here is a code (e.g. "NGN"), not a symbol — format as "NGN 1,000.00"
        Context ctx = new Context();
        ctx.setVariable("senderFullName",   senderFullName);
        ctx.setVariable("receiverFullName", receiverFullName);
        ctx.setVariable("transferAmount",   MoneyFormatter.formatWithCode(currency, transferAmount));
        ctx.setVariable("feeAmount",        MoneyFormatter.formatWithCode(currency, feeAmount));
        ctx.setVariable("balance",          MoneyFormatter.formatWithCode(currency, balance));
        ctx.setVariable("previousBalance",  MoneyFormatter.formatWithCode(currency, previousBalance));
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

        // currencySymbol is the actual symbol (e.g. "₦") — format as "₦1,000,000.00"
        Context ctx = new Context();
        ctx.setVariable("recipientName",     recipientName);
        ctx.setVariable("accountHolder",     accountHolder);
        ctx.setVariable("depositAmount",     MoneyFormatter.format(currencySymbol, depositAmount));
        ctx.setVariable("amount",            MoneyFormatter.format(currencySymbol, amount));
        ctx.setVariable("availableBalance",  MoneyFormatter.format(currencySymbol, availableBalance));
        ctx.setVariable("previousBalance",   MoneyFormatter.format(currencySymbol, previousBalance));
        ctx.setVariable("terminalNumber",    terminalNumber);
        return send(recipientEmail, "ePay — Deposit Successful", "deposit/deposit-success", ctx);
    }

    @Async
    public CompletableFuture<Void> createMaintenanceNotification(MaintenanceDeductionNotification n) {
        // currency here is a code (e.g. "NGN")
        String currency = n.getCurrency();
        Context ctx = new Context();
        ctx.setVariable("userFirstName",     n.getUserFirstName());
        ctx.setVariable("userLastName",      n.getUserLastName());
        ctx.setVariable("feeAmount",         MoneyFormatter.formatWithCode(currency, n.getFeeAmount()));
        ctx.setVariable("availableBalance",  MoneyFormatter.formatWithCode(currency, n.getAvailableBalance()));
        ctx.setVariable("previousBalance",   MoneyFormatter.formatWithCode(currency, n.getPreviousBalance()));
        ctx.setVariable("reason",            n.getReason());
        ctx.setVariable("actionType",        n.getActionType());
        ctx.setVariable("timestamp",         n.getTimestamp());
        ctx.setVariable("success",           n.getSuccess());
        return send(n.getUserEmail(), "ePay — Maintenance Fee Deducted",
                "maintenance/maintenance-fee-deducted", ctx);
    }

    @Async
    public CompletableFuture<Void> createSwapNotification(SwapCurrencyPayload p) {
        // currencySymbol is the symbol of the target currency after swap (e.g. "₦")
        String sym = p.getCurrencySymbol();
        Context ctx = new Context();
        ctx.setVariable("accountHolder",    p.getAccountHolder());
        ctx.setVariable("amount",           MoneyFormatter.format(sym, p.getAmount()));
        ctx.setVariable("availableBalance", MoneyFormatter.format(sym, p.getAvailableBalance()));
        ctx.setVariable("previousBalance",  MoneyFormatter.format(sym, p.getPreviousBalance()));
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
            log.error("[Statement] Failed to send to {}: {}", p.getEmail(), e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<Void> send(String to, String subject, String template, Context ctx) {
        if (to == null || to.isBlank()) {
            log.warn("[WalletNotification] Skipping email — recipient address is null/blank (template={})", template);
            return CompletableFuture.completedFuture(null);
        }
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(templateEngine.process(template, ctx), true);
            javaMailSender.send(mimeMessage);
            log.info("[WalletNotification] Sent '{}' to {}", subject, to);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            log.error("[WalletNotification] Failed to send '{}' to {} (template={}): {}",
                    subject, to, template, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
}
