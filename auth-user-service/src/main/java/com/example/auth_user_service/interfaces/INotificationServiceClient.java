package com.example.auth_user_service.interfaces;

import org.springframework.core.io.ByteArrayResource;

public interface INotificationServiceClient {
    void sendVerificationEmail(String email, String content, String verificationLink, String username);

    Object sendOptEmail(String email, String otp, String restPassword, String configTwoFactorAuth, String configTwoFactorAuthRecovery);

    Object sendPasswordResetMessage(String email, String username, String content, String url);

    void sendBankStatementEmail(String email, String username, ByteArrayResource pdfResource, String period);

    Object sendWelcomeEmail(String recipient, String username, String message);

    Object sendRegistrationOTPMessage(String recipient, String message);
   
    void sendLoginAlertNotification(
            String email, String fullName, String username,
            String loginTime, String ipAddress, String deviceInfo,
            String supportPhone, String supportEmail);

    void sendAccountSecurityAlert(
            String email, String fullName, String username,
            String eventType, String eventTime,
            String ipAddress, String deviceInfo,
            String supportPhone, String supportEmail);

    void sendWalletPinAlert(
            String email, String fullName, String username,
            String action, String actionTime,
            String ipAddress, String deviceInfo,
            String supportPhone, String supportEmail);

    void sendForgotPasswordOtp(String email, String username, String otp);

    void sendForgotUsernameEmail(String email, String username, String fullName);
}
