package com.epay.withdraw.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaystackPayoutGateway implements PayoutGateway {

    private static final String BASE_URL    = "https://api.paystack.co";
    private static final String TRANSFER_PATH = "/transfer";

    @Value("${epay.gateways.paystack.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;

    @Override
    public PayoutResult payout(String reference, String accountNumber, String bankCode,
                                String accountName, BigDecimal amount, String currency,
                                String narration) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(secretKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> recipientBody = Map.of(
                    "type",           "nuban",
                    "name",           accountName != null ? accountName : "Beneficiary",
                    "account_number", accountNumber,
                    "bank_code",      bankCode,
                    "currency",       currency
            );

            ResponseEntity<Map> recipientResp = restTemplate.postForEntity(
                    BASE_URL + "/transferrecipient",
                    new HttpEntity<>(recipientBody, headers), Map.class);

            String recipientCode = null;
            if (recipientResp.getStatusCode().is2xxSuccessful() && recipientResp.getBody() != null) {
                Map<?, ?> data = (Map<?, ?>) recipientResp.getBody().get("data");
                if (data != null) recipientCode = (String) data.get("recipient_code");
            }

            if (recipientCode == null)
                return PayoutResult.builder().success(false)
                        .failureReason("Failed to create transfer recipient").build();

            Map<String, Object> transferBody = Map.of(
                    "source",    "balance",
                    "amount",    amount.multiply(BigDecimal.valueOf(100)).longValue(),
                    "recipient", recipientCode,
                    "reason",    narration != null ? narration : "Withdrawal",
                    "reference", reference
            );

            ResponseEntity<Map> transferResp = restTemplate.postForEntity(
                    BASE_URL + TRANSFER_PATH,
                    new HttpEntity<>(transferBody, headers), Map.class);

            if (transferResp.getStatusCode().is2xxSuccessful() && transferResp.getBody() != null) {
                Map<?, ?> data = (Map<?, ?>) transferResp.getBody().get("data");
                if (data != null) {
                    String status = (String) data.get("status");
                    boolean success = "success".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status);
                    return PayoutResult.builder()
                            .success(success)
                            .gatewayReference((String) data.get("transfer_code"))
                            .failureReason(success ? null : "Transfer status: " + status)
                            .build();
                }
            }

            return PayoutResult.builder().success(false).failureReason("Transfer initiation failed").build();

        } catch (Exception e) {
            log.error("[Paystack] Payout failed ref={}: {}", reference, e.getMessage());
            return PayoutResult.builder().success(false).failureReason("Gateway error: " + e.getMessage()).build();
        }
    }
}
