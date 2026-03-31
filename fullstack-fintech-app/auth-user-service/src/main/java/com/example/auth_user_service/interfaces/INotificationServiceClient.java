package com.example.auth_user_service.interfaces;

import org.springframework.core.io.ByteArrayResource;

public interface INotificationServiceClient {
    void sendVerificationEmail(String email, String content, String verificationLink, String username);

    Object sendOptEmail(String email, String otp, String restPassword, String configTwoFactorAuth, String configTwoFactorAuthRecovery);

    Object sendPasswordResetMessage(String email, String username, String content, String url);

    void sendBankStatementEmail(String email, String username, ByteArrayResource pdfResource, String period);

    Object sendWelcomeEmail(String recipient, String username);

    Object sendRegistrationOTPMessage(String recipient, String message);
    
}
