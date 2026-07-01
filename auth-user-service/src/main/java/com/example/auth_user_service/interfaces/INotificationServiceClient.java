package com.example.auth_user_service.interfaces;

import org.springframework.core.io.ByteArrayResource;

public interface INotificationServiceClient {
    void sendVerificationEmail(String email, String content, String verificationLink, String username);

    Object sendOptEmail(String email, String otp, String restPassword, String configTwoFactorAuth, String configTwoFactorAuthRecovery);

    Object sendPasswordResetMessage(String email, String username, String content, String url);

    void sendBankStatementEmail(String email, String username, ByteArrayResource pdfResource, String period);

    Object sendWelcomeEmail(String recipient, String username, String message);

    Object sendRegistrationOTPMessage(String recipient, String message);

    /**
     * Sends a login security alert via the dedicated login-alert template.
     */
    void sendLoginAlertNotification(
            String email, String fullName, String username,
            String loginTime, String ipAddress, String deviceInfo,
            String supportPhone, String supportEmail);

    /**
     * Sends a security-event notification (password reset/update,
     * deactivate, lock, block, 2FA toggle) via the notification service.
     */
    void sendAccountSecurityAlert(
            String email, String fullName, String username,
            String eventType, String eventTime,
            String ipAddress, String deviceInfo,
            String supportPhone, String supportEmail);

    /**
     * Sends a wallet-PIN set/update notification.
     *
     * @param email        recipient address
     * @param fullName     user's full name
     * @param username     user's login handle
     * @param action       "CREATED" or "UPDATED"
     * @param actionTime   formatted timestamp string
     * @param ipAddress    originating IP
     * @param deviceInfo   device/browser info
     * @param supportPhone support phone number
     * @param supportEmail support email address
     */
    void sendWalletPinAlert(
            String email, String fullName, String username,
            String action, String actionTime,
            String ipAddress, String deviceInfo,
            String supportPhone, String supportEmail);

    /**
     * Sends a 4-digit OTP email to the user as part of the forgot-password flow.
     *
     * @param email    recipient's email address
     * @param username recipient's username (used in greeting)
     * @param otp      the 4-digit OTP to embed in the email
     */
    void sendForgotPasswordOtp(String email, String username, String otp);

    /**
     * Sends the user's username back to their registered email address.
     *
     * @param email    recipient's registered email
     * @param username the username to remind them of
     * @param fullName the user's full name for the greeting
     */
    void sendForgotUsernameEmail(String email, String username, String fullName);
}
