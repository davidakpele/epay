package com.epay.notification.adapter;

import com.epay.common.interfaces.IAuthNotificationPort;
import com.epay.notification.service.AuthenticationNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Implements IAuthNotificationPort from epay-common.
 * auth injects IAuthNotificationPort — never AuthenticationNotificationService directly.
 */
@Component
@RequiredArgsConstructor
public class AuthNotificationAdapter implements IAuthNotificationPort {

    private final AuthenticationNotificationService notificationService;

    @Override
    public CompletableFuture<Void> sendVerificationEmail(String email, String content,
                                                          String link, String username) {
        return notificationService.sendVerificationEmail(email, content, link, username);
    }

    @Override
    public CompletableFuture<Void> sendLoginAlertNotification(String email, String fullName,
                                                               String username, String loginTime,
                                                               String ipAddress, String deviceInfo,
                                                               String supportPhone, String supportEmail) {
        return notificationService.sendLoginAlertNotification(email, fullName, username,
                loginTime, ipAddress, deviceInfo, supportPhone, supportEmail);
    }

    @Override
    public CompletableFuture<Void> sendOptEmail(String email, String otp,
                                                 String resetPasswordUrl,
                                                 String config2faUrl,
                                                 String config2faRecoveryUrl) {
        return notificationService.sendOptEmail(email, otp, resetPasswordUrl,
                config2faUrl, config2faRecoveryUrl);
    }

    @Override
    public CompletableFuture<Void> sendForgotPasswordOtp(String email, String username, String otp) {
        return notificationService.sendForgotPasswordOtp(email, username, otp);
    }

    @Override
    public CompletableFuture<Void> sendAccountSecurityAlert(String email, String fullName,
                                                             String username, String eventType,
                                                             String eventTime, String ipAddress,
                                                             String deviceInfo, String supportPhone,
                                                             String supportEmail) {
        return notificationService.sendAccountSecurityAlert(email, fullName, username,
                eventType, eventTime, ipAddress, deviceInfo, supportPhone, supportEmail);
    }

    @Override
    public CompletableFuture<Void> sendForgotUsernameEmail(String email, String username, String fullName) {
        return notificationService.sendForgotUsernameEmail(email, username, fullName);
    }
}
