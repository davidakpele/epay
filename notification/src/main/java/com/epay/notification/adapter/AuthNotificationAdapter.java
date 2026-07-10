package com.epay.notification.adapter;

import com.epay.common.interfaces.IAuthNotificationPort;
import com.epay.common.interfaces.IAuthNotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Adapts IAuthNotificationPort (legacy synchronous contract)
 * to IAuthNotificationPublisher (RabbitMQ async publisher).
 *
 * Kept for backward compatibility with any consumer still
 * injecting IAuthNotificationPort.
 */
@Component
@RequiredArgsConstructor
public class AuthNotificationAdapter implements IAuthNotificationPort {

    private final IAuthNotificationPublisher publisher;

    @Override
    public CompletableFuture<Void> sendVerificationEmail(String email, String content,
                                                          String link, String username) {
        publisher.publishVerificationEmail(email, content, link, username);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> sendLoginAlertNotification(String email, String fullName,
                                                               String username, String loginTime,
                                                               String ipAddress, String deviceInfo,
                                                               String supportPhone, String supportEmail) {
        publisher.publishLoginAlert(email, fullName, username, loginTime,
                ipAddress, deviceInfo, supportPhone, supportEmail);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> sendOptEmail(String email, String otp,
                                                 String resetPasswordUrl,
                                                 String config2faUrl,
                                                 String config2faRecoveryUrl) {
        publisher.publishTwoFactorOtp(email, otp, resetPasswordUrl, config2faUrl, config2faRecoveryUrl);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> sendForgotPasswordOtp(String email, String username, String otp) {
        publisher.publishForgotPasswordOtp(email, username, otp);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> sendAccountSecurityAlert(String email, String fullName,
                                                             String username, String eventType,
                                                             String eventTime, String ipAddress,
                                                             String deviceInfo, String supportPhone,
                                                             String supportEmail) {
        publisher.publishAccountSecurityAlert(email, fullName, username, eventType,
                eventTime, ipAddress, deviceInfo, supportPhone, supportEmail);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> sendForgotUsernameEmail(String email, String username, String fullName) {
        publisher.publishForgotUsername(email, username, fullName);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> sendBankStatementEmail(String email, String username,
                                                           byte[] pdfBytes, String period) {
        publisher.publishAccountStatement(email, username, pdfBytes, period);
        return CompletableFuture.completedFuture(null);
    }
}