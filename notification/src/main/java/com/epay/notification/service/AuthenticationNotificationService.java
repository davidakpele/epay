package com.epay.notification.service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import jakarta.mail.MessagingException;
import org.springframework.mail.MailException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import jakarta.mail.internet.MimeMessage;
import com.epay.common.config.components.CustomerServiceEmailProperty;
import com.epay.common.config.messaging.RabbitMQConfig;

public class AuthenticationNotificationService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final CustomerServiceEmailProperty customerServiceEmailProperty;

    private static final String CHARACTERS = "4sah3bErz790123456789vdMZ1nsUQ";
    private static final int ID_LENGTH = 10;
    private static final Random random = new Random();

    public AuthenticationNotificationService(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine, CustomerServiceEmailProperty customerServiceEmailProperty) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.customerServiceEmailProperty = customerServiceEmailProperty;
    }

    public static String generateUniqueId() {
        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    // @RabbitListener(queues = RabbitMQConfig.ACCOUNT_VERIFICATION_QUEUE)
    // public void receiveVerificationEmail(AccountVerificationRequest emailRequest) {
    //     if (emailRequest != null) {
    //         sendEmailVerificationMessage(
    //                 emailRequest.getEmail(),
    //                 emailRequest.getmessage(),
    //                 emailRequest.getLink(),
    //                 emailRequest.getUsername());
    //     } else {
    //         System.out.println("Failed to deserialize email request.");
    //     }
    // }

    @SuppressWarnings("null")
    @Async
    public CompletableFuture<Void> sendEmailVerificationMessage(String email, String message, String link,String username) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
            Context context = new Context();
        
            context.setVariable("username", username);
            context.setVariable("link", link);
            context.setVariable("content", message);

            String htmlContent = templateEngine.process("verification-email", context);

            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Account Verification");
            mimeMessageHelper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    // @RabbitListener(queues = RabbitMQConfig.USER_OTP_QUEUE)
    // public void receiveOTPEmail(UserOTPMessage otpRequest) {
    //     if (otpRequest != null) {
    //         sendOTPMessage(
    //                 otpRequest.getEmail(),
    //                 otpRequest.getOtp(),
    //                 otpRequest.getRestPassword(),
    //                 otpRequest.getConfigTwoFactorAuth(),
    //                 otpRequest.getConfigTwoFactorAuthRecovery());
    //     } else {
    //         System.out.println("Failed to deserialize email request.");
    //     }

    // }

    // @SuppressWarnings("null")
    // @Async
    // public CompletableFuture<Void> sendOTPMessage(String email, String otp, String restPassword,
    //         String configTwoFactorAuth,
    //         String configTwoFactorAuthRecovery) {
    //     MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    //     try {
    //         MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

    //         Context context = new Context();
    //         context.setVariable("otp", otp);
    //         context.setVariable("resetPassword", restPassword);
    //         context.setVariable("configuringTwoFactorAuthentication", configTwoFactorAuth);
    //         context.setVariable("configuringTwoFactorAuthenticationRecoveryMethods", configTwoFactorAuth);
    //         String htmlContent = templateEngine.process("verification-otp", context);
    //         mimeMessageHelper.setTo(email);
    //         mimeMessageHelper.setSubject("Verify OTP");
    //         mimeMessageHelper.setText(htmlContent, true);
    //         javaMailSender.send(mimeMessage);
    //         return CompletableFuture.completedFuture(null);
    //     } catch (MessagingException | MailException e) {
    //         throw new MailSendException("Failed to send email: " + e.getMessage(), e);
    //     }
    // }

    // @RabbitListener(queues = RabbitMQConfig.RESET_PASSWORD_QUEUE)
    // public void receivePasswordReset(PasswordResetRequest passwordResetRequest) {
    //     String customerEmail = customerServiceEmailProperty.getEmail();
    //     if (passwordResetRequest != null) {
    //         sendPasswordResetMessage(
    //                 passwordResetRequest.getEmail(),
    //                 passwordResetRequest.getUsername(),
    //                 passwordResetRequest.getmessage(),
    //                 passwordResetRequest.getUrl(),
    //                 customerEmail);
    //     } else {
    //         System.out.println("Failed to deserialize email request.");
    //     }
    // }

    // @SuppressWarnings("null")
    // @Async
    // public CompletableFuture<Void> sendPasswordResetMessage(String email, String username, String content, String url,
    //         String customerEmail) {
    //     MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    //     try {
    //         // Create MimeMessageHelper
    //         MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

    //         // Prepare the HTML template
    //         Context context = new Context();
    //         context.setVariable("username", username);
    //         context.setVariable("link", url);
    //         context.setVariable("content", content);
    //         context.setVariable("supportEmail", customerEmail);
    //         String htmlContent = templateEngine.process("forget-password", context);

    //         // Set email attributes
    //         mimeMessageHelper.setTo(email);
    //         mimeMessageHelper.setSubject("Reset Password");
    //         mimeMessageHelper.setText(htmlContent, true);
    //         // Send the email
    //         javaMailSender.send(mimeMessage);
    //         return CompletableFuture.completedFuture(null);
    //     } catch (MessagingException | MailException e) {
    //         throw new MailSendException("Failed to send email: " + e.getMessage(), e);
    //     }
    // }


    // @RabbitListener(queues = RabbitMQConfig.REGISTRATION_OTP_QUEUE)
    // public void receiveRegistrationOtp(RegistrationOtpMessage registrationOtpMessage) {
    //     System.out.println("From Email Service Check1- Received registration OTP message: " + registrationOtpMessage);
    //     if (registrationOtpMessage != null) {
    //         sendRegistrationOtpMessage(
    //                 registrationOtpMessage.getEmail(),
    //                 registrationOtpMessage.getMessage()
    //         );
    //     } else {
    //         System.out.println("Failed to deserialize email request.");
    //     }
    // }


    // @RabbitListener(queues = RabbitMQConfig.REGISTRATION_OTP_QUEUE)
    // public void receiveWelcomeNotification(WelcomeMessagePayload registrationOtpMessage) {
    //     if (registrationOtpMessage != null) {
    //         sendWelcomeNotification(
    //                 registrationOtpMessage.getEmail(),
    //                 registrationOtpMessage.getUsername(),
    //                 registrationOtpMessage.getMessage()
    //         );
    //     } else {
    //         System.out.println("Failed to deserialize email request.");
    //     }
    // }

    // @SuppressWarnings("null")
    // @Async
    // public CompletableFuture<Void> sendRegistrationOtpMessage(String email, String message) {
    //     MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    //     try {   
    //         // Create MimeMessageHelper
    //         MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
    //         System.out.println("From Email Service Check12 Received registration OTP message: " + message);
    //         // Prepare the HTML template
    //         Context context = new Context();
    //         context.setVariable("message", message);
    //         System.out.println("Processing template with message: " + message);
    //         String htmlContent = templateEngine.process("registration-otp", context);
    //         // Set email attributes
    //         mimeMessageHelper.setTo(email);
    //         mimeMessageHelper.setSubject("Registration OTP");
    //         mimeMessageHelper.setText(htmlContent, true);
    //         System.out.println("Prepared email for: " + email);
    //         System.out.println("Email content: " + message);
    //         // Send the email
    //         javaMailSender.send(mimeMessage);
    //         System.out.println("Email sent successfully to: " + email);
    //         return CompletableFuture.completedFuture(null);
    //     } catch (MessagingException | MailException e) {
    //         throw new MailSendException("Failed to send email: " + e.getMessage(), e);
    //     }   
    // }

    // @SuppressWarnings("null")
    // @Async
    // public CompletableFuture<Void> sendWelcomeNotification(String email, String username, String message) {
    //     MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    //     try {   
    //         MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
    //         Context context = new Context();
    //         context.setVariable("username", username);
    //         context.setVariable("message", message);
    //         String htmlContent = templateEngine.process("success", context);
    //         mimeMessageHelper.setTo(email);
    //         mimeMessageHelper.setSubject("Welcome to Our Service!");
    //         mimeMessageHelper.setText(htmlContent, true);
    //         javaMailSender.send(mimeMessage);
    //         return CompletableFuture.completedFuture(null);
    //     } catch (MessagingException | MailException e) {
    //         throw new MailSendException("Failed to send email: " + e.getMessage(), e);
    //     }

    // }

    // // ── Login Alert ────────────────────────────────────────────────────────────

    // @RabbitListener(queues = RabbitMQConfig.LOGIN_ALERT_QUEUE)
    // public void receiveLoginAlert(LoginAlertNotification payload) {
    //     if (payload != null) {
    //         sendLoginAlertEmail(payload);
    //     } else {
    //         System.out.println("Failed to deserialize login alert request.");
    //     }
    // }

    // @SuppressWarnings("null")
    // @Async
    // public CompletableFuture<Void> sendLoginAlertEmail(LoginAlertNotification payload) {
    //     MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    //     try {
    //         MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
    //         Context context = new Context();
    //         context.setVariable("fullName",     payload.getFullName());
    //         context.setVariable("username",     payload.getUsername());
    //         context.setVariable("loginTime",    payload.getLoginTime());
    //         context.setVariable("ipAddress",    payload.getIpAddress());
    //         context.setVariable("deviceInfo",   payload.getDeviceInfo());
    //         context.setVariable("supportPhone", payload.getSupportPhone());
    //         context.setVariable("supportEmail", payload.getSupportEmail());

    //         String htmlContent = templateEngine.process("login-alert", context);
    //         helper.setTo(payload.getEmail());
    //         helper.setSubject("Online Banking Login — ePay Security Alert");
    //         helper.setText(htmlContent, true);
    //         javaMailSender.send(mimeMessage);
    //         return CompletableFuture.completedFuture(null);
    //     } catch (MessagingException | MailException e) {
    //         throw new MailSendException("Failed to send login alert email: " + e.getMessage(), e);
    //     }
    // }

    // // ── Account Security Events ────────────────────────────────────────────────

    // @RabbitListener(queues = RabbitMQConfig.ACCOUNT_SECURITY_QUEUE)
    // public void receiveAccountSecurityAlert(AccountSecurityNotification payload) {
    //     if (payload != null) {
    //         sendAccountSecurityEmail(payload);
    //     } else {
    //         System.out.println("Failed to deserialize account security notification.");
    //     }
    // }

    // @SuppressWarnings("null")
    // @Async
    // public CompletableFuture<Void> sendAccountSecurityEmail(AccountSecurityNotification payload) {
    //     MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    //     try {
    //         MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
    //         Context context = new Context();
    //         context.setVariable("fullName",     payload.getFullName());
    //         context.setVariable("username",     payload.getUsername());
    //         context.setVariable("eventType",    payload.getEventType());
    //         context.setVariable("eventTime",    payload.getEventTime());
    //         context.setVariable("ipAddress",    payload.getIpAddress());
    //         context.setVariable("deviceInfo",   payload.getDeviceInfo());
    //         context.setVariable("supportPhone", payload.getSupportPhone());
    //         context.setVariable("supportEmail", payload.getSupportEmail());

    //         String htmlContent = templateEngine.process("account-security-alert", context);
    //         helper.setTo(payload.getEmail());
    //         helper.setSubject(resolveSecurityEmailSubject(payload.getEventType()));
    //         helper.setText(htmlContent, true);
    //         javaMailSender.send(mimeMessage);
    //         return CompletableFuture.completedFuture(null);
    //     } catch (MessagingException | MailException e) {
    //         throw new MailSendException("Failed to send account security email: " + e.getMessage(), e);
    //     }
    // }

    // private String resolveSecurityEmailSubject(String eventType) {
    //     if (eventType == null) return "ePay — Account Security Alert";
    //     return switch (eventType.toUpperCase()) {
    //         case "PASSWORD_RESET"      -> "ePay — Password Reset Successful";
    //         case "UPDATE_PASSWORD"     -> "ePay — Password Updated";
    //         case "DEACTIVATE_ACCOUNT"  -> "ePay — Account Deactivated";
    //         case "ACCOUNT_LOCKED"      -> "ePay — Account Locked";
    //         case "ACCOUNT_UNLOCKED"    -> "ePay — Account Unlocked";
    //         case "ACCOUNT_BLOCKED"     -> "ePay — Account Blocked";
    //         case "ACCOUNT_UNBLOCKED"   -> "ePay — Account Unblocked";
    //         case "ACCOUNT_SUSPENDED"   -> "ePay — Account Suspended";
    //         case "TWO_FACTOR_ENABLED"  -> "ePay — Two-Factor Authentication Enabled";
    //         case "TWO_FACTOR_DISABLED" -> "ePay — Two-Factor Authentication Disabled";
    //         default                    -> "ePay — Account Security Alert";
    //     };
    // }

    // // ── Forgot Password OTP ────────────────────────────────────────────────────

    // @RabbitListener(queues = RabbitMQConfig.FORGOT_PASSWORD_OTP_QUEUE)
    // public void receiveForgotPasswordOtp(ForgotPasswordOtpPayload payload) {
    //     if (payload != null) {
    //         sendForgotPasswordOtpEmail(payload);
    //     } else {
    //         System.out.println("Failed to deserialize forgot-password OTP request.");
    //     }
    // }

    // @SuppressWarnings("null")
    // @Async
    // public CompletableFuture<Void> sendForgotPasswordOtpEmail(ForgotPasswordOtpPayload payload) {
    //     MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    //     try {
    //         MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
    //         Context context = new Context();
    //         context.setVariable("username", payload.getUsername());
    //         context.setVariable("otp",      payload.getOtp());

    //         String htmlContent = templateEngine.process("forgot-password-otp", context);
    //         helper.setTo(payload.getEmail());
    //         helper.setSubject("ePay — Your Password Reset Code");
    //         helper.setText(htmlContent, true);
    //         javaMailSender.send(mimeMessage);
    //         return CompletableFuture.completedFuture(null);
    //     } catch (MessagingException | MailException e) {
    //         throw new MailSendException("Failed to send forgot-password OTP email: " + e.getMessage(), e);
    //     }
    // }

    // // ── Forgot Username ────────────────────────────────────────────────────────

    // @RabbitListener(queues = RabbitMQConfig.FORGOT_USERNAME_QUEUE)
    // public void receiveForgotUsername(ForgotUsernamePayload payload) {
    //     if (payload != null) {
    //         sendForgotUsernameEmail(payload);
    //     } else {
    //         System.out.println("Failed to deserialize forgot-username request.");
    //     }
    // }

    // @SuppressWarnings("null")
    // @Async
    // public CompletableFuture<Void> sendForgotUsernameEmail(ForgotUsernamePayload payload) {
    //     MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    //     try {
    //         MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
    //         Context context = new Context();
    //         context.setVariable("fullName", payload.getFullName());
    //         context.setVariable("username", payload.getUsername());

    //         String htmlContent = templateEngine.process("forgot-username", context);
    //         helper.setTo(payload.getEmail());
    //         helper.setSubject("ePay — Your Username");
    //         helper.setText(htmlContent, true);
    //         javaMailSender.send(mimeMessage);
    //         return CompletableFuture.completedFuture(null);
    //     } catch (MessagingException | MailException e) {
    //         throw new MailSendException("Failed to send forgot-username email: " + e.getMessage(), e);
    //     }
    // }
}