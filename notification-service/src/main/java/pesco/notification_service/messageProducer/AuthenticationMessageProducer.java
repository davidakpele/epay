package pesco.notification_service.messageProducer;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pesco.notification_service.configurations.RabbitMQConfig;
import pesco.notification_service.payloads.AccountVerificationRequest;
import pesco.notification_service.payloads.LoginAlertNotification;
import pesco.notification_service.payloads.PasswordResetRequest;
import pesco.notification_service.payloads.RegistrationOtpMessage;
import pesco.notification_service.payloads.UserOTPMessage;
import pesco.notification_service.payloads.WelcomeMessagePayload;

@Service
public class AuthenticationMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public AuthenticationMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * Sends a notification to user to complete their sign-up
     * @param email
     * @param message
     * @param link
     * @param username
     */
    public void sendVerificationEmail(String email, String message, String link, String username) {
        AccountVerificationRequest emailRequest = new AccountVerificationRequest(email, message, link, username);
        rabbitTemplate.convertAndSend(RabbitMQConfig.AUTH_EXCHANGE, RabbitMQConfig.ROUTING_KEY_ACCOUNT_VERIFICATION, emailRequest);
    }
   
    /**
     * Sends otp notification to user
     * 
     * @param email
     * @param otp
     * @param restPassword
     * @param configTwoFactorAuth
     * @param configTwoFactorAuthRecovery
     */
    public void sendOptEmail(String email, String otp, String restPassword, String configTwoFactorAuth, String configTwoFactorAuthRecovery) {
        UserOTPMessage userOTPMessage = new UserOTPMessage(email, otp, restPassword, configTwoFactorAuth, configTwoFactorAuthRecovery);
        rabbitTemplate.convertAndSend(RabbitMQConfig.AUTH_EXCHANGE, RabbitMQConfig.ROUTING_KEY_ACCOUNT_USER_OTP, userOTPMessage);
    }
    
    /**
     * Send a link to user to reset their password
     * 
     * @param email
     * @param username
     * @param message
     * @param url
     */
    public void sendPasswordResetEmail(String email, String username, String message, String url) {
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest(email, username, message, url);
        rabbitTemplate.convertAndSend(RabbitMQConfig.AUTH_EXCHANGE, RabbitMQConfig.ROUTING_KEY_RESET_PASSWORD, passwordResetRequest);
    }
    
    /**
     * Sends registration OTP to user
     * 
     * @param email
     * @param otp
     */
    public void sendRegistrationOtpMessage(String email, String otp) {
        RegistrationOtpMessage request = new RegistrationOtpMessage(email, otp);
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.AUTH_EXCHANGE, 
                RabbitMQConfig.ROUTING_KEY_REGISTRATION_OTP, 
                request
            );
        } catch (AmqpException e) {
            throw e;
        }
    }

    /**
     * Sends welcome message to user after successful registration
     * 
     * @param email
     * @param username
     * @param message
     */
    public void sendWelcomeNotification(String email, String username, String message) {
        WelcomeMessagePayload request = new WelcomeMessagePayload(email, username, message);
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.AUTH_EXCHANGE, 
                RabbitMQConfig.ROUTING_KEY_WELCOME_MESSAGE, 
                request
            );
        } catch (AmqpException e) {
            throw e;
        }
    }

    /**
     * Sends a login security alert email to the user after successful login.
     * Contains: login time, IP address, device info, support contacts.
     */
    public void sendLoginAlert(LoginAlertNotification payload) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.AUTH_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_LOGIN_ALERT,
                payload
            );
        } catch (AmqpException e) {
            throw e;
        }
    }

    /**
     * Sends a security-event notification email (password reset, update password,
     * deactivate account, lock, block, 2FA toggle).
     *
     * @param payload {@link pesco.notification_service.payloads.AccountSecurityNotification}
     */
    public void sendAccountSecurityAlert(pesco.notification_service.payloads.AccountSecurityNotification payload) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.AUTH_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_ACCOUNT_SECURITY,
                payload
            );
        } catch (AmqpException e) {
            throw e;
        }
    }

    /**
     * Sends a 4-digit OTP email for forgot-password flow.
     *
     * @param payload {@link pesco.notification_service.payloads.ForgotPasswordOtpPayload}
     */
    public void sendForgotPasswordOtp(pesco.notification_service.payloads.ForgotPasswordOtpPayload payload) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.AUTH_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_FORGOT_PASSWORD_OTP,
                payload
            );
        } catch (AmqpException e) {
            throw e;
        }
    }

    /**
     * Sends a forgot-username email — reminds the user of their username.
     *
     * @param payload {@link pesco.notification_service.payloads.ForgotUsernamePayload}
     */
    public void sendForgotUsername(pesco.notification_service.payloads.ForgotUsernamePayload payload) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.AUTH_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_FORGOT_USERNAME,
                payload
            );
        } catch (AmqpException e) {
            throw e;
        }
    }
}
