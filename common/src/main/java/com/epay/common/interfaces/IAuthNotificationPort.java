package com.epay.common.interfaces;

import java.util.concurrent.CompletableFuture;

public interface IAuthNotificationPort {

    CompletableFuture<Void> sendVerificationEmail(String email, String content,
                                                   String link, String username);

    CompletableFuture<Void> sendLoginAlertNotification(String email, String fullName,
                                                        String username, String loginTime,
                                                        String ipAddress, String deviceInfo,
                                                        String supportPhone, String supportEmail);

    CompletableFuture<Void> sendOptEmail(String email, String otp,
                                          String resetPasswordUrl,
                                          String config2faUrl,
                                          String config2faRecoveryUrl);

    CompletableFuture<Void> sendForgotPasswordOtp(String email, String username, String otp);

    CompletableFuture<Void> sendAccountSecurityAlert(String email, String fullName,
                                                      String username, String eventType,
                                                      String eventTime, String ipAddress,
                                                      String deviceInfo, String supportPhone,
                                                      String supportEmail);

    CompletableFuture<Void> sendForgotUsernameEmail(String email, String username, String fullName);
}
