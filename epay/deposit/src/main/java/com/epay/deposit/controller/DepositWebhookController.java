package com.epay.deposit.controller;

import com.epay.deposit.service.DepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Deposit Webhooks", description = "Receive payment-gateway callbacks for deposit events")
@Slf4j
@RestController
@RequestMapping("/webhook/deposit")
@RequiredArgsConstructor
public class DepositWebhookController {

    private final DepositService depositService;

    @Operation(
        summary     = "Paystack deposit webhook",
        description = "Receives HMAC-signed webhook events from Paystack and credits the user's wallet on charge.success."
    )
    @PostMapping("/paystack")
    public ResponseEntity<Void> paystackWebhook(
            @RequestHeader(value = "X-Paystack-Signature", required = false) String signature,
            @RequestBody String rawPayload) {

        String reference = extractReference(rawPayload);
        if (reference == null) {
            log.warn("[Webhook/Paystack] Missing reference in payload");
            return ResponseEntity.badRequest().build();
        }
        try {
            depositService.handleWebhook(signature, rawPayload, reference, "PAYSTACK");
        } catch (Exception e) {
            log.error("[Webhook/Paystack] ref={} error={}", reference, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary     = "Flutterwave deposit webhook",
        description = "Receives hash-verified webhook events from Flutterwave and credits the user's wallet on successful charge."
    )
    @PostMapping("/flutterwave")
    public ResponseEntity<Void> flutterwaveWebhook(
            @RequestHeader(value = "verif-hash", required = false) String signature,
            @RequestBody String rawPayload) {

        String reference = extractReference(rawPayload);
        if (reference == null) {
            log.warn("[Webhook/Flutterwave] Missing reference in payload");
            return ResponseEntity.badRequest().build();
        }
        try {
            depositService.handleWebhook(signature, rawPayload, reference, "FLUTTERWAVE");
        } catch (Exception e) {
            log.error("[Webhook/Flutterwave] ref={} error={}", reference, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    private String extractReference(String rawPayload) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            Map<?, ?> body = mapper.readValue(rawPayload, Map.class);
            Object data = body.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object ref = dataMap.get("reference");
                return ref != null ? ref.toString() : null;
            }
        } catch (Exception e) {
            log.warn("[Webhook] Could not extract reference: {}", e.getMessage());
        }
        return null;
    }
}
