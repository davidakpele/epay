package com.epay.common.interfaces;

/**
 * Publisher interface for auth-related notifications.
 * Defined in common — implemented in notification via RabbitMQ.
 * Inject this in auth, not AuthenticationNotificationService directly.
 */
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
}
