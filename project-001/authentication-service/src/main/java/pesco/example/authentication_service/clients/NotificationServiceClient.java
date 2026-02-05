package pesco.example.authentication_service.clients;

import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pesco.example.authentication_service.exceptions.Extraction;
import pesco.example.authentication_service.exceptions.UserClientNotFoundException;
import reactor.core.publisher.Mono;

@Service
public class NotificationServiceClient {
    
    private final WebClient notificationServiceWebClient;
    private final Extraction extraction;

    public NotificationServiceClient(WebClient notificationServiceWebClient, Extraction extraction) {
        this.notificationServiceWebClient = notificationServiceWebClient;
        this.extraction = extraction;
    }

    public void sendVerificationEmail(String email, String content, String verificationLink, String username) {
         try {
             // Create the request body
             Map<String, Object> requestBody = new HashMap<>();
             requestBody.put("email", email);
             requestBody.put("username", username);
             requestBody.put("link", verificationLink);
             requestBody.put("message", content);

             this.notificationServiceWebClient.post()
                     .uri("/send/verification-message")
                     .bodyValue(requestBody)
                     .retrieve()
                     .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                             clientResponse -> clientResponse.bodyToMono(String.class)
                                     .flatMap(errorMessage -> {
                                         if (clientResponse.statusCode().is4xxClientError()) {
                                             String details = extraction.extractDetailsFromError(errorMessage);
                                             return Mono.error(
                                                     new UserClientNotFoundException("Notification failed", details));
                                         }
                                         return Mono
                                                 .error(new RuntimeException(
                                                         "Server error while sending notification"));
                                     }))
                     .toBodilessEntity()
                     .block();

         } catch (Exception ex) {
             System.err.println("Error sending notification: " + ex.getMessage());
         }
     }
    
    public Object sendOptEmail(String email, String otp, String restPassword, String configTwoFactorAuth,
            String configTwoFactorAuthRecovery) {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("otp", otp);
            requestBody.put("restPassword", restPassword);
            requestBody.put("configTwoFactorAuth", configTwoFactorAuth);
            requestBody.put("configTwoFactorAuthRecovery", configTwoFactorAuthRecovery);

            // Send the POST request
            return notificationServiceWebClient.post()
                    .uri("/send/otp-message")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extraction.extractDetailsFromError(errorMessage);
                                            return Mono.error(
                                                    new UserClientNotFoundException("OTP email failed", details));
                                        }
                                        return Mono
                                                .error(new RuntimeException(
                                                        "Server error while sending OTP email"));
                                    }))
                    .bodyToMono(Object.class)
                    .block();
        } catch (Exception ex) {
            // Handle exceptions
            System.err.println("Error sending OTP notifications: " + ex.getMessage());
            return null;
        }
    }

    public Object sendPasswordResetMessage(String email, String username, String content, String url) {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("username", username);
            requestBody.put("message", content);
            requestBody.put("url", url);
        
            // Send the POST request
            return notificationServiceWebClient.post()
                    .uri("/send/password-reset-message")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extraction.extractDetailsFromError(errorMessage);
                                            return Mono.error(
                                                    new UserClientNotFoundException("Password reset email failed", details));
                                        }
                                        return Mono
                                                .error(new RuntimeException(
                                                        "Server error while sending password reset email"));
                                    }))
                    .bodyToMono(Object.class)
                    .block();
        } catch (Exception ex) {
            // Handle exceptions
            System.err.println("Error sending password reset notifications: " + ex.getMessage());
            return null;
        }
    }

    public void sendBankStatementEmail(String email, String username, ByteArrayResource pdfResource, String period) {
        try {
            String filename = "bank-statement.pdf";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("email", email);
            body.add("username", username);
            body.add("subject", "Your Bank Statement - PESCO BANK");
            body.add("pdfFile", pdfResource);
            body.add("filename", filename);
            body.add("period", period);

            this.notificationServiceWebClient.post()
                    .uri("/send/bank-statement")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extraction.extractDetailsFromError(errorMessage);
                                            return Mono.error(
                                                    new UserClientNotFoundException("Bank statement email failed", details));
                                        }
                                        return Mono
                                                .error(new RuntimeException(
                                                        "Server error while sending bank statement"));
                                    }))
                    .toBodilessEntity()
                    .block();

            System.out.println("Bank statement email sent successfully to: " + email);

        } catch (Exception ex) {
            System.err.println("Error sending bank statement email: " + ex.getMessage());
            throw new RuntimeException("Failed to send bank statement email", ex);
        }
    }

    public Object sendWelcomeEmail(String recipient, String username) {
        String content = "Welcome " + username + "! Your account has been verified successfully. Welcome aboard!";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", recipient);
        requestBody.put("username", username);
        requestBody.put("message", content);
         this.notificationServiceWebClient.post()
                     .uri("/send/welcome-message")
                     .bodyValue(requestBody)
                     .retrieve()
                     .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                             clientResponse -> clientResponse.bodyToMono(String.class)
                                     .flatMap(errorMessage -> {
                                         if (clientResponse.statusCode().is4xxClientError()) {
                                             String details = extraction.extractDetailsFromError(errorMessage);
                                             return Mono.error(
                                                     new UserClientNotFoundException("Notification failed", details));
                                         }
                                         return Mono
                                                 .error(new RuntimeException(
                                                         "Server error while sending notification"));
                                     }))
                     .toBodilessEntity()
                     .block();
        return null;
    }

    public Object sendRegistrationOTPMessage(String recipient, String message) {
        try {
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("recipient", recipient);
            requestBody.put("message", message);

            // Send the POST request
            return notificationServiceWebClient.post()
                    .uri("/send/registration-otp-message")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extraction.extractDetailsFromError(errorMessage);
                                            return Mono.error(
                                                    new UserClientNotFoundException("Custom message failed", details));
                                        }
                                        return Mono
                                                .error(new RuntimeException(
                                                        "Server error while sending custom message"));
                                    }))
                    .bodyToMono(Object.class)
                    .block();
        } catch (Exception ex) {
            // Handle exceptions
            System.err.println("Error sending custom message: " + ex.getMessage());
            return null;
        }
    }
}

