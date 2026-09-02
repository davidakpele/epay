package com.epay.withdraw.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Paystack payout gateway.
 *
 * Transfer status values returned by Paystack:
 *   success   — money sent and confirmed
 *   pending   — queued, will process soon (treat as success — webhook confirms later)
 *   otp       — Paystack dashboard OTP verification required (2FA on transfers).
 *               This is a Paystack account setting. Disable it in:
 *               Paystack Dashboard → Settings → Preferences → Transfer OTP
 *               OR enable the finalizeWithOtp flow to handle it programmatically.
 *   failed    — transfer rejected
 *   reversed  — transfer reversed by Paystack
 *
 * For TEST keys Paystack always requires OTP unless disabled in dashboard.
 * For LIVE keys the default can vary based on account configuration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaystackPayoutGateway implements PayoutGateway {

    private static final String BASE_URL      = "https://api.paystack.co";
    private static final String TRANSFER_PATH = "/transfer";
    private static final String FINALIZE_PATH = "/transfer/finalize_transfer";

    @Value("${epay.gateways.paystack.secret-key}")
    private String secretKey;

    /**
     * OTP to finalize transfers when Paystack requires it.
     * For TEST mode Paystack always accepts "123456" as the OTP.
     * For LIVE mode set this via env var PAYSTACK_TRANSFER_OTP.
     * Set this to blank/empty if OTP is disabled on your Paystack account.
     */
    @Value("${epay.gateways.paystack.transfer-otp:123456}")
    private String transferOtp;

    private final RestTemplate restTemplate;

    @Override
    public PayoutResult payout(String reference, String accountNumber, String bankCode,
                                String accountName, BigDecimal amount, String currency,
                                String narration) {
        try {
            HttpHeaders headers = bearerHeaders();

            // ── Step 1: Create transfer recipient ─────────────────────────────
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

            String recipientCode = extractRecipientCode(recipientResp);
            if (recipientCode == null) {
                log.error("[Paystack] Failed to create recipient ref={}", reference);
                return PayoutResult.builder().success(false)
                        .failureReason("Failed to create transfer recipient").build();
            }

            // ── Step 2: Initiate transfer ─────────────────────────────────────
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

            if (!transferResp.getStatusCode().is2xxSuccessful()
                    || transferResp.getBody() == null) {
                return PayoutResult.builder().success(false)
                        .failureReason("Transfer initiation failed").build();
            }

            Map<?, ?> data = (Map<?, ?>) transferResp.getBody().get("data");
            if (data == null)
                return PayoutResult.builder().success(false)
                        .failureReason("Empty transfer response").build();

            String status       = (String) data.get("status");
            String transferCode = (String) data.get("transfer_code");

            log.info("[Paystack] Transfer initiated ref={} transferCode={} status={}",
                    reference, transferCode, status);

            // ── Step 3: Handle OTP challenge ──────────────────────────────────
            if ("otp".equalsIgnoreCase(status)) {
                if (transferOtp == null || transferOtp.isBlank()) {
                    // OTP required but none configured — tell the admin to either
                    // disable Transfer OTP in Paystack dashboard or configure the OTP env var
                    log.warn("[Paystack] OTP required for transfer but PAYSTACK_TRANSFER_OTP is not set. "
                            + "Disable Transfer OTP in Paystack Dashboard → Settings → Preferences, "
                            + "or set PAYSTACK_TRANSFER_OTP env var. ref={}", reference);
                    return PayoutResult.builder()
                            .success(false)
                            .gatewayReference(transferCode)
                            .failureReason("Paystack OTP verification required. "
                                    + "Disable 'Transfer OTP' in your Paystack dashboard settings "
                                    + "or contact your platform administrator.")
                            .build();
                }
                // Finalize with the configured OTP
                return finalizeTransfer(transferCode, reference);
            }

            // ── Step 4: Evaluate final status ─────────────────────────────────
            boolean success = "success".equalsIgnoreCase(status)
                    || "pending".equalsIgnoreCase(status);

            return PayoutResult.builder()
                    .success(success)
                    .gatewayReference(transferCode)
                    .failureReason(success ? null : "Transfer status: " + status)
                    .build();

        } catch (Exception e) {
            log.error("[Paystack] Payout failed ref={}: {}", reference, e.getMessage());
            return PayoutResult.builder()
                    .success(false)
                    .failureReason("Gateway error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Finalize a transfer that is pending OTP verification.
     * Paystack TEST mode always accepts "123456".
     */
    private PayoutResult finalizeTransfer(String transferCode, String reference) {
        try {
            log.info("[Paystack] Finalizing OTP transfer ref={} code={}", reference, transferCode);

            Map<String, Object> finalizeBody = Map.of(
                    "transfer_code", transferCode,
                    "otp",           transferOtp
            );

            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    BASE_URL + FINALIZE_PATH,
                    new HttpEntity<>(finalizeBody, bearerHeaders()),
                    Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Map<?, ?> data = (Map<?, ?>) resp.getBody().get("data");
                if (data != null) {
                    String status = (String) data.get("status");
                    boolean success = "success".equalsIgnoreCase(status)
                            || "pending".equalsIgnoreCase(status);
                    log.info("[Paystack] Finalize result ref={} status={}", reference, status);
                    return PayoutResult.builder()
                            .success(success)
                            .gatewayReference(transferCode)
                            .failureReason(success ? null : "Finalize status: " + status)
                            .build();
                }
            }
            return PayoutResult.builder()
                    .success(false)
                    .gatewayReference(transferCode)
                    .failureReason("OTP finalization failed — empty response")
                    .build();

        } catch (Exception e) {
            log.error("[Paystack] Finalize failed ref={}: {}", reference, e.getMessage());
            return PayoutResult.builder()
                    .success(false)
                    .gatewayReference(transferCode)
                    .failureReason("OTP finalization error: " + e.getMessage())
                    .build();
        }
    }

    private String extractRecipientCode(ResponseEntity<Map> response) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
            if (data != null) return (String) data.get("recipient_code");
        }
        return null;
    }

    private HttpHeaders bearerHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(secretKey);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
