package com.epay.common.config.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaystackClient {

    private static final String BASE_URL   = "https://api.paystack.co";
    private static final String INIT_PATH  = "/transaction/initialize";

    @Value("${epay.gateways.paystack.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;

    /**
     *
     *
     * @param reference   your unique transaction reference
     * @param email       the customer's email address (required by Paystack)
     * @param amount      amount in the major currency unit (e.g. 500.00 NGN); converted to kobo internally
     * @param currency    ISO 4217 currency code (e.g. "NGN")
     * @param callbackUrl URL Paystack redirects to after payment (may be null)
     * @param metadata    optional custom metadata to attach to the Paystack transaction (may be null)
     * @return map with {@code authorization_url}, {@code access_code}, {@code reference}
     * @throws RuntimeException if Paystack returns a non-2xx response or an empty data body
     */
    public Map<String, Object> initializeTransaction(String reference,
                                                     String email,
                                                     BigDecimal amount,
                                                     String currency,
                                                     String callbackUrl,
                                                     Map<String, Object> metadata) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("reference", reference);
        body.put("email",     email);
        body.put("amount",    amount.multiply(BigDecimal.valueOf(100)).longValue());
        body.put("currency",  currency);
        if (callbackUrl != null && !callbackUrl.isBlank()) {
            body.put("callback_url", callbackUrl);
        }
        if (metadata != null && !metadata.isEmpty()) {
            body.put("metadata", metadata);
        }

        HttpEntity<LinkedHashMap<String, Object>> request = new HttpEntity<>(body, bearerHeaders());

        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    BASE_URL + INIT_PATH, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("authorization_url", data.get("authorization_url"));
                    result.put("access_code",       data.get("access_code"));
                    result.put("reference",         data.get("reference"));
                    log.info("[PaystackClient] Transaction initialized ref={} accessCode={}",
                            reference, data.get("access_code"));
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("[PaystackClient] initializeTransaction failed ref={}: {}", reference, e.getMessage());
            throw new RuntimeException(
                    "Paystack transaction initialization failed for reference: " + reference, e);
        }

        throw new RuntimeException(
                "Paystack transaction initialization returned empty data for reference: " + reference);
    }

    private HttpHeaders bearerHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
