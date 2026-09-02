package com.epay.notification.listener;

import com.epay.common.config.messaging.RabbitMQConfig;
import com.epay.domain.notification.input.AccountSecurityNotification;
import com.epay.domain.notification.input.AccountVerificationNotification;
import com.epay.domain.notification.input.ForgotPasswordOtpNotification;
import com.epay.domain.notification.input.ForgotUsernameNotification;
import com.epay.domain.notification.input.LoginAlertNotification;
import com.epay.domain.notification.input.OTPOnSignUp;
import com.epay.domain.notification.input.TwoFactorOtpNotification;
import com.epay.notification.service.AuthenticationNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthNotificationListener {

    private final AuthenticationNotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_VERIFICATION_QUEUE)
    public void onVerificationEmail(AccountVerificationNotification payload) {
        if (payload == null) { log.warn("[AuthListener] Null payload on verification queue"); return; }
        notificationService.sendVerificationEmail(
                payload.getEmail(), payload.getContent(),
                payload.getLink(), payload.getUsername());
    }

    @RabbitListener(queues = RabbitMQConfig.LOGIN_ALERT_QUEUE)
    public void onLoginAlert(LoginAlertNotification payload) {
        if (payload == null) { log.warn("[AuthListener] Null payload on login-alert queue"); return; }
        notificationService.sendLoginAlertNotification(
                payload.getEmail(), payload.getFullName(), payload.getUsername(),
                payload.getLoginTime(), payload.getIpAddress(), payload.getDeviceInfo(),
                payload.getSupportPhone(), payload.getSupportEmail());
    }

    @RabbitListener(queues = RabbitMQConfig.USER_OTP_QUEUE)
    public void onTwoFactorOtp(TwoFactorOtpNotification payload) {
        if (payload == null) { log.warn("[AuthListener] Null payload on user-otp queue"); return; }
        notificationService.sendOptEmail(
                payload.getEmail(), payload.getOtp(),
                payload.getResetPasswordUrl(), payload.getConfig2faUrl(),
                payload.getConfig2faRecoveryUrl());
    }

    @RabbitListener(queues = RabbitMQConfig.USER_SIGNUP_QUEUE)
    public void oTPOnSignUp(OTPOnSignUp payload) {
        if (payload == null) { log.warn("[AuthListener] Null payload on user-signup-otp queue"); return; }
        notificationService.sendVerificationOptEmail(payload.getEmail(), payload.getOtp());
    }

    @RabbitListener(queues = RabbitMQConfig.FORGOT_PASSWORD_OTP_QUEUE)
    public void onForgotPasswordOtp(ForgotPasswordOtpNotification payload) {
        if (payload == null) { log.warn("[AuthListener] Null payload on forgot-password queue"); return; }
        notificationService.sendForgotPasswordOtp(
                payload.getEmail(), payload.getUsername(), payload.getOtp());
    }

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_SECURITY_QUEUE)
    public void onAccountSecurity(AccountSecurityNotification payload) {
        if (payload == null) { log.warn("[AuthListener] Null payload on account-security queue"); return; }
        notificationService.sendAccountSecurityAlert(
                payload.getEmail(), payload.getFullName(), payload.getUsername(),
                payload.getEventType(), payload.getEventTime(),
                payload.getIpAddress(), payload.getDeviceInfo(),
                payload.getSupportPhone(), payload.getSupportEmail());
    }

    @RabbitListener(queues = RabbitMQConfig.FORGOT_USERNAME_QUEUE)
    public void onForgotUsername(ForgotUsernameNotification payload) {
        if (payload == null) { log.warn("[AuthListener] Null payload on forgot-username queue"); return; }
        notificationService.sendForgotUsernameEmail(
                payload.getEmail(), payload.getUsername(), payload.getFullName());
    }
}
