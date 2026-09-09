package com.epay.withdraw.controller;

import com.epay.common.interfaces.IHistoryPort;
import com.epay.common.interfaces.IIdempotencyPort;
import com.epay.common.interfaces.IWithdrawWalletPort;
import com.epay.common.interfaces.UserLookupPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;


@Tag(name = "Withdrawal Webhooks", description = "Receive Paystack callbacks for withdrawal transfer events")
@Slf4j
@RestController
@RequestMapping("/webhook/withdraw")
@RequiredArgsConstructor
public class WithdrawWebhookController {

    private static final long IDEM_TTL_SEC = 86_400L;

    @Value("${epay.gateways.paystack.secret-key:}")
    private String paystackSecretKey;

    private final IHistoryPort        historyPort;
    private final IWithdrawWalletPort walletPort;
    private final IIdempotencyPort    idempotencyPort;
    private final UserLookupPort      userLookupPort;
    private final ObjectMapper        objectMapper;

    @Operation(
        summary     = "Paystack withdrawal webhook",
        description = "Receives HMAC-verified Paystack events for transfer.success, transfer.failed, transfer.reversed and charge.success. Handles wallet debits and auto-refunds."
    )
    @PostMapping("/paystack")
    public ResponseEntity<Void> paystackTransferWebhook(
            @RequestHeader(value = "X-Paystack-Signature", required = false) String signature,
            @RequestBody String rawPayload) {

        try {
            if (!validateSignature(rawPayload, signature)) {
                log.warn("[Webhook/Withdraw] Invalid Paystack signature");
                return ResponseEntity.badRequest().build();
            }

            Map<?, ?> body = objectMapper.readValue(rawPayload, Map.class);
            String event = (String) body.get("event");
            if (event == null) {
                log.warn("[Webhook/Withdraw] Missing event field");
                return ResponseEntity.ok().build();
            }

            Map<?, ?> data = (Map<?, ?>) body.get("data");
            if (data == null) {
                log.warn("[Webhook/Withdraw] Missing data field for event={}", event);
                return ResponseEntity.ok().build();
            }

            String reference       = extractString(data, "reference");
            String transferCode    = extractString(data, "transfer_code");
            String gatewayStatus   = extractString(data, "status");
            String failureReason   = extractString(data, "reason");
            Number amountKobo      = (Number) data.get("amount");
            BigDecimal amount      = amountKobo != null
                    ? BigDecimal.valueOf(amountKobo.longValue()).divide(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            if (reference == null) {
                log.warn("[Webhook/Withdraw] No reference in transfer event={}", event);
                return ResponseEntity.ok().build();
            }

            String idemKey = "webhook:withdraw:" + reference + ":" + event;
            if (idempotencyPort.exists(idemKey)) {
                log.info("[Webhook/Withdraw] Duplicate event ignored: ref={} event={}", reference, event);
                return ResponseEntity.ok().build();
            }

            log.info("[Webhook/Withdraw] Processing event={} ref={} status={}", event, reference, gatewayStatus);

            switch (event) {
                case "charge.success"    -> handleChargeSuccess(data, reference, amount);
                case "transfer.success"  -> handleTransferSuccess(reference, transferCode, amount);
                case "transfer.failed"   -> handleTransferFailed(reference, amount, failureReason);
                case "transfer.reversed" -> handleTransferReversed(reference, amount, failureReason);
                default -> log.debug("[Webhook/Withdraw] Unhandled event: {}", event);
            }

            idempotencyPort.store(idemKey, IDEM_TTL_SEC);

        } catch (Exception e) {
            log.error("[Webhook/Withdraw] Uncaught error: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    private void handleChargeSuccess(Map<?, ?> data, String reference, BigDecimal amount) {
        log.info("[Webhook/Withdraw] charge.success ref={} amount={}", reference, amount);

        if (reference == null || !reference.startsWith("WDR_")) {
            log.debug("[Webhook/Withdraw] charge.success ignored — not a withdrawal reference: {}", reference);
            return;
        }

        Long userId = extractUserIdFromReference(reference);
        if (userId == null) {
            log.error("[Webhook/Withdraw] charge.success — cannot extract userId from ref={}", reference);
            return;
        }

        if (!walletPort.walletExists(userId)) {
            log.error("[Webhook/Withdraw] charge.success — wallet not found for userId={} ref={}", userId, reference);
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[Webhook/Withdraw] charge.success — zero/negative amount ref={} — debit skipped", reference);
            return;
        }

        String debitIdemKey = "webhook:withdraw:debit:" + reference;
        if (idempotencyPort.exists(debitIdemKey)) {
            log.info("[Webhook/Withdraw] charge.success debit already applied for ref={}", reference);
            return;
        }

        try {

            String currency = "NGN";
            Object currencyObj = data.get("currency");
            if (currencyObj != null && !currencyObj.toString().isBlank()) {
                currency = currencyObj.toString().toUpperCase();
            }

            walletPort.debitWallet(userId, currency, amount, reference);
            idempotencyPort.store(debitIdemKey, IDEM_TTL_SEC);

            log.info("[Webhook/Withdraw] Wallet debited userId={} amount={} {} ref={}",
                    userId, amount, currency, reference);

            historyPort.advanceStatus(
                    reference,
                    "DELIVERED",
                    "paystack-webhook",
                    "Payment confirmed via Paystack checkout. Wallet debited.",
                    null, null, null);

        } catch (Exception e) {
            log.error("[Webhook/Withdraw] charge.success debit FAILED ref={} userId={}: {}",
                    reference, userId, e.getMessage(), e);
        }
    }

    private void handleTransferSuccess(String reference, String transferCode, BigDecimal amount) {
        try {
            historyPort.advanceStatus(
                    reference,
                    "SETTLED",
                    "paystack-webhook",
                    "Transfer confirmed by Paystack. Transfer code: " + transferCode,
                    null, null, null);
            log.info("[Webhook/Withdraw] Transfer SUCCESS: ref={} transferCode={} amount={}",
                    reference, transferCode, amount);
        } catch (Exception e) {
            log.error("[Webhook/Withdraw] Failed to advance status for ref={}: {}", reference, e.getMessage());
        }
    }

    private void handleTransferFailed(String reference, BigDecimal amount, String reason) {
        log.warn("[Webhook/Withdraw] Transfer FAILED: ref={} reason={}", reference, reason);
        try {
            historyPort.advanceStatus(
                    reference, "FAILED",
                    "paystack-webhook",
                    "Transfer failed: " + reason,
                    null, null, reason);
        } catch (Exception e) {
            log.error("[Webhook/Withdraw] History update failed for ref={}: {}", reference, e.getMessage());
        }
        issueRefund(reference, amount, reason, "FAILED");
    }

    private void handleTransferReversed(String reference, BigDecimal amount, String reason) {
        log.warn("[Webhook/Withdraw] Transfer REVERSED: ref={} reason={}", reference, reason);
        try {
            historyPort.advanceStatus(
                    reference, "REVERSED",
                    "paystack-webhook",
                    "Transfer reversed by Paystack: " + reason,
                    null, null, reason);
        } catch (Exception e) {
            log.error("[Webhook/Withdraw] History update failed for ref={}: {}", reference, e.getMessage());
        }
        issueRefund(reference, amount, reason, "REVERSED");
    }

    private Long extractUserIdFromReference(String reference) {
        try {
            String[] parts = reference.split("_");
            if (parts.length >= 4) return Long.parseLong(parts[2]);
        } catch (NumberFormatException ignored) {}
        return null;
    }

    private void issueRefund(String reference, BigDecimal amount, String reason, String eventType) {
        Long userId = extractUserIdFromReference(reference);
        if (userId == null) {
            log.error("[Webhook/Withdraw] Cannot extract userId from ref={} — refund skipped", reference);
            return;
        }

        if (!walletPort.walletExists(userId)) {
            log.error("[Webhook/Withdraw] Wallet not found for userId={} — refund skipped", userId);
            return;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[Webhook/Withdraw] Zero/negative amount for refund ref={} — skipped", reference);
            return;
        }

        String refundIdemKey = "refund:withdraw:" + reference;
        if (idempotencyPort.exists(refundIdemKey)) {
            log.info("[Webhook/Withdraw] Refund already issued for ref={}", reference);
            return;
        }

        try {
            String refundRef = reference + "_WH_REFUND";
            walletPort.refundWallet(userId, "NGN", amount, refundRef);
            idempotencyPort.store(refundIdemKey, IDEM_TTL_SEC);
            log.info("[Webhook/Withdraw] Refund issued: userId={} amount={} ref={} reason={}",
                    userId, amount, refundRef, reason);
            historyPort.advanceStatus(
                    reference, "REVERSED",
                    "paystack-webhook",
                    "Auto-refund issued after " + eventType + ": " + reason,
                    null, null, reason);
        } catch (Exception e) {
            log.error("[Webhook/Withdraw] Refund FAILED for ref={}: {}", reference, e.getMessage());
        }
    }

    private boolean validateSignature(String payload, String signature) {
        if (signature == null || paystackSecretKey == null || paystackSecretKey.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(
                    paystackSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            String computed = HexFormat.of().formatHex(
                    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return computed.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("[Webhook/Withdraw] Signature validation error: {}", e.getMessage());
            return false;
        }
    }

    private String extractString(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
