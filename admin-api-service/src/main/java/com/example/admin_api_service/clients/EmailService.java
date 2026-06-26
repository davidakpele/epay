package com.example.admin_api_service.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.admin_api_service.dto.EmailAttachment;
import com.example.admin_api_service.dto.EmailDeliveryStatus;
import com.example.admin_api_service.exceptions.EmailServiceException;
import com.example.admin_api_service.payloads.BulkEmailRequest;
import com.example.admin_api_service.payloads.EmailRequest;
import com.example.admin_api_service.responses.EmailResponse;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Qualifier("notificationServiceWebClient")
    private final WebClient notificationServiceWebClient;

    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_BACKOFF = Duration.ofSeconds(1);

    /**
     * Send email synchronously (blocking)
     */
    public EmailResponse sendEmail(String to, String subject, String body) {
        try {
            EmailRequest request = EmailRequest.builder()
                    .to(to)
                    .subject(subject)
                    .body(body)
                    .build();

            return notificationServiceWebClient.post()
                    .uri("/api/notifications/email/send")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        log.error("Client error sending email to {}: {}", to, response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new EmailServiceException("Client error: " + error)));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response -> {
                        log.error("Server error sending email to {}: {}", to, response.statusCode());
                        return response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new EmailServiceException("Server error: " + error)));
                    })
                    .bodyToMono(EmailResponse.class)
                    .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_BACKOFF)
                            .filter(throwable -> throwable instanceof EmailServiceException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                throw new EmailServiceException("Failed to send email after " + MAX_RETRIES + " retries");
                            }))
                    .block();

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            return EmailResponse.builder()
                    .success(false)
                    .message("Failed to send email: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Send email asynchronously (non-blocking)
     */
    public CompletableFuture<EmailResponse> sendEmailAsync(String to, String subject, String body) {
        EmailRequest request = EmailRequest.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .build();

        return notificationServiceWebClient.post()
                .uri("/api/notifications/email/send")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    log.error("Client error sending email to {}: {}", to, response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(error -> Mono.error(new EmailServiceException("Client error: " + error)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    log.error("Server error sending email to {}: {}", to, response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(error -> Mono.error(new EmailServiceException("Server error: " + error)));
                })
                .bodyToMono(EmailResponse.class)
                .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_BACKOFF)
                        .filter(throwable -> throwable instanceof EmailServiceException)
                        .doBeforeRetry(retrySignal -> 
                            log.warn("Retrying email to {}, attempt {}", to, retrySignal.totalRetries() + 1)))
                .doOnSuccess(response -> log.info("Email sent successfully to {}", to))
                .doOnError(error -> log.error("Failed to send email to {}: {}", to, error.getMessage()))
                .toFuture();
    }

    /**
     * Send HTML email
     */
    public EmailResponse sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            EmailRequest request = EmailRequest.builder()
                    .to(to)
                    .subject(subject)
                    .body(htmlBody)
                    .html(true)
                    .build();

            return notificationServiceWebClient.post()
                    .uri("/api/notifications/email/send-html")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EmailResponse.class)
                    .block();

        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            return EmailResponse.builder()
                    .success(false)
                    .message("Failed to send HTML email: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Send email with attachments
     */
    public EmailResponse sendEmailWithAttachments(String to, String subject, String body, 
                                                 List<EmailAttachment> attachments) {
        try {
            EmailRequest request = EmailRequest.builder()
                    .to(to)
                    .subject(subject)
                    .body(body)
                    .attachments(attachments)
                    .build();

            return notificationServiceWebClient.post()
                    .uri("/api/notifications/email/send-with-attachments")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EmailResponse.class)
                    .block();

        } catch (Exception e) {
            log.error("Failed to send email with attachments to {}: {}", to, e.getMessage());
            return EmailResponse.builder()
                    .success(false)
                    .message("Failed to send email with attachments: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Send bulk emails
     */
    public EmailResponse sendBulkEmails(List<String> recipients, String subject, String body) {
        try {
            BulkEmailRequest request = BulkEmailRequest.builder()
                    .recipients(recipients)
                    .subject(subject)
                    .body(body)
                    .build();

            return notificationServiceWebClient.post()
                    .uri("/api/notifications/email/bulk-send")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EmailResponse.class)
                    .block();

        } catch (Exception e) {
            log.error("Failed to send bulk emails: {}", e.getMessage());
            return EmailResponse.builder()
                    .success(false)
                    .message("Failed to send bulk emails: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Get email delivery status
     */
    public EmailDeliveryStatus getEmailStatus(String emailId) {
        try {
            return notificationServiceWebClient.get()
                    .uri("/api/notifications/email/status/{emailId}", emailId)
                    .retrieve()
                    .bodyToMono(EmailDeliveryStatus.class)
                    .block();

        } catch (Exception e) {
            log.error("Failed to get email status for {}: {}", emailId, e.getMessage());
            return EmailDeliveryStatus.builder()
                    .emailId(emailId)
                    .status("UNKNOWN")
                    .message("Failed to retrieve status: " + e.getMessage())
                    .build();
        }
    }
}