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
     * Sends a security-event notification (login alert, password reset/update,
     * deactivate, lock, block, 2FA toggle) via the notification service.
     *
     * @param email        recipient address
     * @param fullName     user's full name
     * @param username     user's login handle
     * @param eventType    one of: LOGIN_ALERT | PASSWORD_RESET | UPDATE_PASSWORD |
     *                     DEACTIVATE_ACCOUNT | ACCOUNT_LOCKED | ACCOUNT_UNLOCKED |
     *                     ACCOUNT_BLOCKED | ACCOUNT_UNBLOCKED |
     *                     TWO_FACTOR_ENABLED | TWO_FACTOR_DISABLED
     * @param eventTime    formatted timestamp string
     * @param ipAddress    originating IP (may be null/empty)
     * @param deviceInfo   device/browser info (may be null/empty)
     * @param supportPhone support phone number
     * @param supportEmail support email address
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
}
