package com.example.auth_user_service.httpClients;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.auth_user_service.exceptions.Extraction;
import com.example.auth_user_service.exceptions.UserClientNotFoundException;
import com.example.auth_user_service.interfaces.INotificationServiceClient;

import reactor.core.publisher.Mono;

public class NotificationServiceClient implements INotificationServiceClient {

    private static final Logger log = Logger.getLogger(NotificationServiceClient.class.getName());

    private final WebClient notificationServiceWebClient;
    private final Extraction extraction;

    public NotificationServiceClient(WebClient notificationServiceWebClient, Extraction extraction) {
        this.notificationServiceWebClient = notificationServiceWebClient;
        this.extraction = extraction;
    }

    @Override
    public void sendVerificationEmail(String email, String content, String verificationLink, String username) {
        try {
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
                                            return Mono.error(new UserClientNotFoundException("Notification failed", details));
                                        }
                                        return Mono.error(new RuntimeException("Server error while sending notification"));
                                    }))
                    .toBodilessEntity()
                    .doOnSuccess(response -> log.info("[RESPONSE] POST /send/verification-message | status: "
                            + response.getStatusCode()))
                    .block();
        } catch (Exception ex) {
            log.severe("[EXCEPTION] POST /send/verification-message | error: " + ex.getMessage());
        }
    }

    @Override
    public Object sendOptEmail(String email, String otp, String restPassword, String configTwoFactorAuth,
            String configTwoFactorAuthRecovery) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("otp", otp);
        requestBody.put("restPassword", restPassword);
        requestBody.put("configTwoFactorAuth", configTwoFactorAuth);
        requestBody.put("configTwoFactorAuthRecovery", configTwoFactorAuthRecovery);

        System.out.println("[REQUEST] POST /send/otp-message | body: " + requestBody);

        try {
            return notificationServiceWebClient.post()
                    .uri("/send/otp-message")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                System.err.println("[RESPONSE ERROR] POST /send/otp-message | status: "
                                        + clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                        .defaultIfEmpty("[empty body]")
                                        .flatMap(errorMessage -> {
                                            System.err.println("[RESPONSE ERROR BODY] POST /send/otp-message | body: "
                                                    + errorMessage);
                                            if (clientResponse.statusCode().is4xxClientError()) {
                                                String details = extraction.extractDetailsFromError(errorMessage);
                                                return Mono.error(
                                                        new UserClientNotFoundException("OTP email failed", details));
                                            }
                                            return Mono.error(
                                                    new RuntimeException("Server error while sending OTP email: "
                                                            + errorMessage));
                                        });
                            })
                    .bodyToMono(String.class)  // ← fix
                    .doOnSuccess(res -> System.out.println("[RESPONSE SUCCESS] POST /send/otp-message | body: " + res))
                    .doOnError(err -> System.err.println("[RESPONSE ERROR] POST /send/otp-message | error type: "
                            + err.getClass().getSimpleName() + " | message: " + err.getMessage()))
                    .block();
        } catch (Exception ex) {
            System.err.println("[EXCEPTION] POST /send/otp-message | error: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to send OTP email", ex);
        }
    }

    @Override
    public Object sendPasswordResetMessage(String email, String username, String content, String url) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("username", username);
            requestBody.put("message", content);
            requestBody.put("url", url);

            log.info("[REQUEST] POST /send/password-reset-message | body: " + requestBody);

            Object response = notificationServiceWebClient.post()
                    .uri("/send/password-reset-message")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        log.warning("[RESPONSE ERROR] POST /send/password-reset-message | status: "
                                                + clientResponse.statusCode() + " | body: " + errorMessage);
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extraction.extractDetailsFromError(errorMessage);
                                            return Mono.error(new UserClientNotFoundException("Password reset email failed", details));
                                        }
                                        return Mono.error(new RuntimeException("Server error while sending password reset email"));
                                    }))
                    .bodyToMono(Object.class)
                    .doOnSuccess(res -> log.info("[RESPONSE] POST /send/password-reset-message | body: " + res))
                    .block();

            return response;
        } catch (Exception ex) {
            log.severe("[EXCEPTION] POST /send/password-reset-message | error: " + ex.getMessage());
            return null;
        }
    }

    @Override
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

            log.info("[REQUEST] POST /send/bank-statement | email: " + email
                    + " | username: " + username + " | period: " + period + " | filename: " + filename);

            this.notificationServiceWebClient.post()
                    .uri("/send/bank-statement")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        log.warning("[RESPONSE ERROR] POST /send/bank-statement | status: "
                                                + clientResponse.statusCode() + " | body: " + errorMessage);
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extraction.extractDetailsFromError(errorMessage);
                                            return Mono.error(new UserClientNotFoundException("Bank statement email failed", details));
                                        }
                                        return Mono.error(new RuntimeException("Server error while sending bank statement"));
                                    }))
                    .toBodilessEntity()
                    .doOnSuccess(response -> log.info("[RESPONSE] POST /send/bank-statement | status: "
                            + response.getStatusCode()))
                    .block();

        } catch (Exception ex) {
            log.severe("[EXCEPTION] POST /send/bank-statement | error: " + ex.getMessage());
            throw new RuntimeException("Failed to send bank statement email", ex);
        }
    }

    @Override
    public Object sendWelcomeEmail(String recipient, String username) {
        String content = "Welcome " + username + "! Your account has been verified successfully. Welcome aboard!";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", recipient);
        requestBody.put("username", username);
        requestBody.put("message", content);

        log.info("[REQUEST] POST /send/welcome-message | body: " + requestBody);

        try {
            this.notificationServiceWebClient.post()
                    .uri("/send/welcome-message")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        log.warning("[RESPONSE ERROR] POST /send/welcome-message | status: "
                                                + clientResponse.statusCode() + " | body: " + errorMessage);
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extraction.extractDetailsFromError(errorMessage);
                                            return Mono.error(new UserClientNotFoundException("Notification failed", details));
                                        }
                                        return Mono.error(new RuntimeException("Server error while sending notification"));
                                    }))
                    .toBodilessEntity()
                    .doOnSuccess(response -> log.info("[RESPONSE] POST /send/welcome-message | status: "
                            + response.getStatusCode()))
                    .block();
        } catch (Exception ex) {
            log.severe("[EXCEPTION] POST /send/welcome-message | error: " + ex.getMessage());
        }

        return null;
    }

    @Override
    public Object sendRegistrationOTPMessage(String recipient, String message) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", recipient);
            requestBody.put("message", message);

            log.info("[REQUEST] POST /send/registration-otp-message | body: " + requestBody);

            Object response = notificationServiceWebClient.post()
                    .uri("/send/registration-otp-message")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        log.warning("[RESPONSE ERROR] POST /send/registration-otp-message | status: "
                                                + clientResponse.statusCode() + " | body: " + errorMessage);
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extraction.extractDetailsFromError(errorMessage);
                                            return Mono.error(new UserClientNotFoundException("Custom message failed", details));
                                        }
                                        return Mono.error(new RuntimeException("Server error while sending custom message"));
                                    }))
                    .bodyToMono(Object.class)
                    .doOnSuccess(res -> log.info("[RESPONSE] POST /send/registration-otp-message | body: " + res))
                    .block();

            return response;
        } catch (Exception ex) {
            log.severe("[EXCEPTION] POST /send/registration-otp-message | error: " + ex.getMessage());
            return null;
        }
    }
}