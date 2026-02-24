package pesco.notification_service.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;
import pesco.notification_service.configurations.RabbitMQConfig;
import pesco.notification_service.payloads.AccountVerificationRequest;
import pesco.notification_service.payloads.PasswordResetRequest;
import pesco.notification_service.payloads.RegistrationOtpMessage;
import pesco.notification_service.payloads.UserOTPMessage;
import pesco.notification_service.utils.CustomerServiceEmailProperty;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.springframework.mail.MailException;

import jakarta.mail.MessagingException;

@Service
public class AuthenticationEmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final CustomerServiceEmailProperty customerServiceEmailProperty;

    private static final String CHARACTERS = "4sah3bErz790123456789vdMZ1nsUQ";
    private static final int ID_LENGTH = 10;
    private static final Random random = new Random();

    public AuthenticationEmailService(
            JavaMailSender javaMailSender,
            SpringTemplateEngine templateEngine,
            CustomerServiceEmailProperty customerServiceEmailProperty
    ) {
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

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_VERIFICATION_QUEUE)
    public void receiveVerificationEmail(AccountVerificationRequest emailRequest) {
        if (emailRequest != null) {
            sendEmailVerificationMessage(
                    emailRequest.getEmail(),
                    emailRequest.getmessage(),
                    emailRequest.getLink(),
                    emailRequest.getUsername());
        } else {
            System.out.println("Failed to deserialize email request.");
        }
    }

    @Async
    public CompletableFuture<Void> sendEmailVerificationMessage(String email, String message, String link,
            String username) {
        // Create MimeMessage
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            // Create MimeMessageHelper
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
            // Prepare the HTML template
            Context context = new Context();
        
            context.setVariable("username", username);
            context.setVariable("link", link);
            context.setVariable("content", message);

            String htmlContent = templateEngine.process("verification-email", context);

            // Set email attributes
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Account Verification");
            mimeMessageHelper.setText(htmlContent, true);
            // Send the email
            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.USER_OTP_QUEUE)
    public void receiveOTPEmail(UserOTPMessage otpRequest) {
        if (otpRequest != null) {
            sendOTPMessage(
                    otpRequest.getEmail(),
                    otpRequest.getOtp(),
                    otpRequest.getRestPassword(),
                    otpRequest.getConfigTwoFactorAuth(),
                    otpRequest.getConfigTwoFactorAuthRecovery());
        } else {
            System.out.println("Failed to deserialize email request.");
        }

    }

    @Async
    public CompletableFuture<Void> sendOTPMessage(String email, String otp, String restPassword,
            String configTwoFactorAuth,
            String configTwoFactorAuthRecovery) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            // Create MimeMessageHelper
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Prepare the HTML template
            Context context = new Context();
            context.setVariable("otp", otp);
            context.setVariable("resetPassword", restPassword);
            context.setVariable("configuringTwoFactorAuthentication", configTwoFactorAuth);
            context.setVariable("configuringTwoFactorAuthenticationRecoveryMethods", configTwoFactorAuth);
            String htmlContent = templateEngine.process("verification-otp", context);

            // Set email attributes
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Verify OTP");
            mimeMessageHelper.setText(htmlContent, true);
            // Send the email
            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.RESET_PASSWORD_QUEUE)
    public void receivePasswordReset(PasswordResetRequest passwordResetRequest) {
        String customerEmail = customerServiceEmailProperty.getEmail();
        if (passwordResetRequest != null) {
            sendPasswordResetMessage(
                    passwordResetRequest.getEmail(),
                    passwordResetRequest.getUsername(),
                    passwordResetRequest.getmessage(),
                    passwordResetRequest.getUrl(),
                    customerEmail);
        } else {
            System.out.println("Failed to deserialize email request.");
        }
    }

    @Async
    public CompletableFuture<Void> sendPasswordResetMessage(String email, String username, String content, String url,
            String customerEmail) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            // Create MimeMessageHelper
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Prepare the HTML template
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("link", url);
            context.setVariable("content", content);
            context.setVariable("supportEmail", customerEmail);
            String htmlContent = templateEngine.process("forget-password", context);

            // Set email attributes
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Reset Password");
            mimeMessageHelper.setText(htmlContent, true);
            // Send the email
            javaMailSender.send(mimeMessage);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }


    @RabbitListener(queues = RabbitMQConfig.REGISTRATION_OTP_QUEUE)
    public void receiveRegistrationOtp(RegistrationOtpMessage registrationOtpMessage) {
        System.out.println("From Email Service Check1- Received registration OTP message: " + registrationOtpMessage);
        if (registrationOtpMessage != null) {
            sendRegistrationOtpMessage(
                    registrationOtpMessage.getEmail(),
                    registrationOtpMessage.getMessage()
            );
        } else {
            System.out.println("Failed to deserialize email request.");
        }
    }

    @Async
    public CompletableFuture<Void> sendRegistrationOtpMessage(String email, String message) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {   
            // Create MimeMessageHelper
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
            System.out.println("From Email Service Check12 Received registration OTP message: " + message);
            // Prepare the HTML template
            Context context = new Context();
            context.setVariable("message", message);
            System.out.println("Processing template with message: " + message);
            String htmlContent = templateEngine.process("registration-otp", context);
            // Set email attributes
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("Registration OTP");
            mimeMessageHelper.setText(htmlContent, true);
            System.out.println("Prepared email for: " + email);
            System.out.println("Email content: " + message);
            // Send the email
            javaMailSender.send(mimeMessage);
            System.out.println("Email sent successfully to: " + email);
            return CompletableFuture.completedFuture(null);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }   
    }

}
