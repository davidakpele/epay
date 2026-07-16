package com.epay.notification.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationNotificationService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public CompletableFuture<Void> sendVerificationEmail(String email, String content,
                                                          String link, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("link", link);
        context.setVariable("content", content);
        return send(email, "Account Verification", "verification-email", context);
    }

    @Async
    public CompletableFuture<Void> sendLoginAlertNotification(String email, String fullName,
                                                               String username, String loginTime,
                                                               String ipAddress, String deviceInfo,
                                                               String supportPhone, String supportEmail) {
        Context context = new Context();
        context.setVariable("fullName",     fullName);
        context.setVariable("username",     username);
        context.setVariable("loginTime",    loginTime);
        context.setVariable("ipAddress",    ipAddress);
        context.setVariable("deviceInfo",   deviceInfo);
        context.setVariable("supportPhone", supportPhone);
        context.setVariable("supportEmail", supportEmail);
        return send(email, "ePay — Login Alert", "auth/login-alert", context);
    }

    @Async
    public CompletableFuture<Void> sendOptEmail(String email, String otp,
                                                 String resetPasswordUrl,
                                                 String config2faUrl,
                                                 String config2faRecoveryUrl) {
        Context context = new Context();
        context.setVariable("otp",                                     otp);
        context.setVariable("resetPassword",                           resetPasswordUrl);
        context.setVariable("configuringTwoFactorAuthentication",      config2faUrl);
        context.setVariable("configuringTwoFactorAuthenticationRecoveryMethods", config2faRecoveryUrl);
        return send(email, "ePay — Your Two-Factor Authentication Code", "auth/forgot-password-otp", context);
    }

    @Async
    public CompletableFuture<Void> sendForgotPasswordOtp(String email, String username, String otp) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("otp",      otp);
        return send(email, "ePay — Your Password Reset Code", "auth/forgot-password-otp", context);
    }

    @Async
    public CompletableFuture<Void> sendAccountSecurityAlert(String email, String fullName,
                                                             String username, String eventType,
                                                             String eventTime, String ipAddress,
                                                             String deviceInfo, String supportPhone,
                                                             String supportEmail) {
        Context context = new Context();
        context.setVariable("fullName",     fullName);
        context.setVariable("username",     username);
        context.setVariable("eventType",    eventType);
        context.setVariable("eventTime",    eventTime);
        context.setVariable("ipAddress",    ipAddress);
        context.setVariable("deviceInfo",   deviceInfo);
        context.setVariable("supportPhone", supportPhone);
        context.setVariable("supportEmail", supportEmail);

        String subject = resolveSecuritySubject(eventType);
        return send(email, subject, "auth/account-security-alert", context);
    }

    @Async
    public CompletableFuture<Void> sendForgotUsernameEmail(String email, String username, String fullName) {
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("username", username);
        return send(email, "ePay — Your Username", "auth/forgot-username", context);
    }

    @Async
    public CompletableFuture<Void> sendBankStatementEmail(String email, String username,
                                                           byte[] pdfBytes, String period) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("period",   period);
            String html = templateEngine.process("auth/account-statement", context);
            helper.setTo(email);
            helper.setSubject("ePay — Your Account Statement");
            helper.setText(html, true);
            helper.addAttachment(
                    "account-statement-" + period.replace(" ", "-") + ".pdf",
                    new org.springframework.core.io.ByteArrayResource(pdfBytes),
                    "application/pdf");
            javaMailSender.send(mimeMessage);
            log.info("Account statement sent to {}", email);
            return CompletableFuture.completedFuture(null);
        } catch (jakarta.mail.MessagingException | org.springframework.mail.MailException e) {
            log.error("[Email] Failed to send statement to {}: {}", email, e.getMessage());
            // Never rethrow — mail failure must not roll back caller transaction
        }
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> send(String to, String subject, String template, Context context) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            String html = templateEngine.process(template, context);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            javaMailSender.send(mimeMessage);
            log.info("[Email] Sent '{}' to {}", subject, to);
        } catch (MessagingException | MailException e) {
            log.error("[Email] Failed to send '{}' to {}: {}", subject, to, e.getMessage());
            // Never rethrow — mail failure must not roll back caller transaction
        }
        return CompletableFuture.completedFuture(null);
    }

    private String resolveSecuritySubject(String eventType) {
        if (eventType == null) return "ePay — Account Security Alert";
        return switch (eventType.toUpperCase()) {
            case "PASSWORD_RESET"         -> "ePay — Password Reset Successful";
            case "PASSWORD_CHANGED"       -> "ePay — Password Updated";
            case "TWO_FACTOR_ENABLED"     -> "ePay — Two-Factor Authentication Enabled";
            case "TWO_FACTOR_DISABLED"    -> "ePay — Two-Factor Authentication Disabled";
            case "ACCOUNT_LOCKED"         -> "ePay — Account Locked";
            case "ACCOUNT_UNLOCKED"       -> "ePay — Account Unlocked";
            case "ACCOUNT_DELETION_REQUESTED" -> "ePay — Account Deletion Requested";
            case "SUSPICIOUS_LOGIN_DETECTED"  -> "ePay — Suspicious Login Detected";
            default                       -> "ePay — Account Security Alert";
        };
    }

    @Async
    public CompletableFuture<Void> sendVerificationOptEmail(String email, String otp) {
        Context context = new Context();
        context.setVariable("otp", otp);
        return send(email, "ePay — Verify Your Account", "auth/signup-otp", context);
    }
}
