package com.epay.deposit.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaystackDepositGateway implements DepositGateway {

    private static final String BASE_URL      = "https://api.paystack.co";
    private static final String INIT_PATH     = "/transaction/initialize";
    private static final String VERIFY_PATH   = "/transaction/verify/";

    @Value("${epay.gateways.paystack.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;

    @Override
    public String initiate(String reference, String email, BigDecimal amount,
                           String currency, String callbackUrl) {
        HttpHeaders headers = bearerHeaders();
        Map<String, Object> body = Map.of(
                "reference",    reference,
                "email",        email,
                "amount",       amount.multiply(BigDecimal.valueOf(100)).longValue(), // kobo/cents
                "currency",     currency,
                "callback_url", callbackUrl != null ? callbackUrl : ""
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(BASE_URL + INIT_PATH, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
            if (data != null) return (String) data.get("authorization_url");
        }
        throw new RuntimeException("Paystack initiation failed for reference: " + reference);
    }

    @Override
    public DepositVerificationResult verify(String reference) {
        HttpEntity<Void> request = new HttpEntity<>(bearerHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_URL + VERIFY_PATH + reference, HttpMethod.GET, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
            if (data != null) {
                String status = (String) data.get("status");
                boolean success = "success".equalsIgnoreCase(status);
                Number amountKobo = (Number) data.get("amount");
                BigDecimal amount = amountKobo != null
                        ? BigDecimal.valueOf(amountKobo.longValue()).divide(BigDecimal.valueOf(100))
                        : BigDecimal.ZERO;
                return DepositVerificationResult.builder()
                        .success(success)
                        .gatewayReference((String) data.get("id"))
                        .amount(amount)
                        .currency((String) data.get("currency"))
                        .failureReason(success ? null : "Gateway status: " + status)
                        .build();
            }
        }
        return DepositVerificationResult.builder().success(false)
                .failureReason("Verification request failed").build();
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            String computed = HexFormat.of().formatHex(
                    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return computed.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("Paystack signature validation error: {}", e.getMessage());
            return false;
        }
    }

    private HttpHeaders bearerHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(secretKey);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
