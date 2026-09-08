package com.epay.deposit.controller;

import com.epay.deposit.service.DepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/webhook/deposit")
@RequiredArgsConstructor
public class DepositWebhookController {

    private final DepositService depositService;

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
