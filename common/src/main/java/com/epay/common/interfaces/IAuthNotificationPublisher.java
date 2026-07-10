package com.epay.common.interfaces;

public interface IAuthNotificationPublisher {

    void publishVerificationEmail(String email, String content, String link, String username);

    void publishLoginAlert(String email, String fullName, String username,
                           String loginTime, String ipAddress, String deviceInfo,
                           String supportPhone, String supportEmail);

    void publishTwoFactorOtp(String email, String otp,
                              String resetPasswordUrl, String config2faUrl,
                              String config2faRecoveryUrl);

    void publishForgotPasswordOtp(String email, String username, String otp);

    void publishAccountSecurityAlert(String email, String fullName, String username,
                                     String eventType, String eventTime,
                                     String ipAddress, String deviceInfo,
                                     String supportPhone, String supportEmail);

    void publishForgotUsername(String email, String username, String fullName);

    /**
     * Sends an account statement PDF to the user's email.
     *
     * @param email     recipient email
     * @param username  recipient username (for greeting)
     * @param pdfBytes  generated PDF bytes
     * @param period    statement period string e.g. "01-Jan-2026 to 30-Jun-2026"
     */
    void publishAccountStatement(String email, String username, byte[] pdfBytes, String period);
}
