package com.epay.deposit.service;

import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.DepositException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.interfaces.IDepositHistoryPort;
import com.epay.common.interfaces.IDepositWalletPort;
import com.epay.common.interfaces.IIdempotencyPort;
import com.epay.common.interfaces.IWalletNotificationPublisher;
import com.epay.common.interfaces.UserLookupPort;
import com.epay.deposit.gateway.DepositGateway;
import com.epay.deposit.gateway.DepositGatewayFactory;
import com.epay.deposit.gateway.DepositVerificationResult;
import com.epay.domain.deposit.dto.DepositDTO;
import com.epay.domain.deposit.enums.DepositAndWithdrawSystem;
import com.epay.domain.deposit.enums.DepositStatus;
import com.epay.domain.deposit.input.InitiateDepositRequest;
import com.epay.domain.deposit.input.VerifyDepositRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {

    private static final String TX_PREFIX    = "NX";
    private static final long   WEBHOOK_IDEM_TTL = 86_400L;  // 24 h dedup window

    private final DepositGatewayFactory        gatewayFactory;
    private final IDepositWalletPort           walletPort;
    private final IDepositHistoryPort          historyPort;
    private final IWalletNotificationPublisher notificationPublisher;
    private final UserLookupPort               userLookupPort;
    private final IIdempotencyPort             idempotencyPort;

    public ResponseEntity<?> createDeposit(InitiateDepositRequest request) {

        Long userId = request.getUserId();

        if (!userLookupPort.existsActiveUser(userId))
            return error("User not found or account inactive", HttpStatus.NOT_FOUND,
                    "The account associated with this request does not exist or is inactive.");

        if (!walletPort.walletExists(userId))
            return error("Wallet not found", HttpStatus.NOT_FOUND,
                    "No wallet found for this account. Please create a wallet first.");

        String currency = request.getCurrency().toUpperCase();
        if (!walletPort.isCurrencySupported(currency))
            return error("Unsupported currency: " + currency, HttpStatus.BAD_REQUEST,
                    "This currency is not supported. Please choose a supported currency.");

        if (request.getDepositSystem() == null)
            return error("Deposit system is required.", HttpStatus.BAD_REQUEST,
                    "Please provide a depositSystem: PAYSTACK, CARD, or USSD");

        return switch (request.getDepositSystem()) {
            case PAYSTACK    -> processDeposit(request, userId, currency, "PAYSTACK");
            case CARD        -> processDeposit(request, userId, currency, "CARD");
            case USSD        -> processDeposit(request, userId, currency, "USSD");
            case FLUTTERWAVE -> processDeposit(request, userId, currency, "FLUTTERWAVE");
            default          -> error("Unsupported deposit system.", HttpStatus.BAD_REQUEST,
                    "Use PAYSTACK, CARD, USSD, or FLUTTERWAVE.");
        };
    }

    public DepositDTO verify(Long userId, VerifyDepositRequest request) {
        if (!userLookupPort.existsActiveUser(userId))
            throw new ResourceNotFoundException("User not found or account inactive");

        Long referenceUserId = extractUserIdFromReference(request.getReference());
        if (referenceUserId != null && !referenceUserId.equals(userId))
            throw new BadRequestException("Reference does not belong to this account",
                    ErrorCode.INVALID_INPUT);

        DepositGateway gateway = resolveGatewayFromReference(request.getReference());
        DepositVerificationResult result = callGatewayVerify(gateway, request.getReference());
        return processVerificationResult(userId, request.getReference(), null, result);
    }

    public void handleWebhook(String signature, String rawPayload,
                               String reference, String channel) {
        // ── Idempotency guard — prevents double-credit on gateway retries ─────
        String idemKey = "webhook:deposit:" + reference;
        if (idempotencyPort.exists(idemKey)) {
            log.info("[Webhook] Duplicate delivery ignored: ref={} channel={}", reference, channel);
            return;
        }

        DepositGateway gateway = resolveGatewayFromChannel(channel);

        if (!gateway.validateWebhookSignature(rawPayload, signature)) {
            log.warn("[Webhook] Invalid signature for ref={} channel={}", reference, channel);
            throw new BadRequestException("Invalid webhook signature", ErrorCode.INVALID_INPUT);
        }

        Long userId = extractUserIdFromReference(reference);
        if (userId == null) {
            log.error("[Webhook] Cannot extract userId from ref={}", reference);
            return;
        }

        DepositVerificationResult result = callGatewayVerify(gateway, reference);
        processVerificationResult(userId, reference, channel, result);

        // Store idempotency key AFTER successful processing
        idempotencyPort.store(idemKey, WEBHOOK_IDEM_TTL);
        log.info("[Webhook] Processed ref={} success={}", reference, result.isSuccess());
    }

    private ResponseEntity<?> processDeposit(InitiateDepositRequest request, Long userId, String currency, String channel) {
        Long walletId       = walletPort.getWalletId(userId);
        String currencySymbol = walletPort.getCurrencySymbol(currency);
        BigDecimal previousBalance = walletPort.getBalance(userId, currency);

        boolean credited = creditWallet(userId, currency, request.getAmount(),
                generateReference(request.getDepositSystem(), userId));
        if (!credited)
            return error("Failed to credit wallet. Transaction aborted.",
                    HttpStatus.INTERNAL_SERVER_ERROR, "Please try again or contact support.");

        BigDecimal newBalance = walletPort.getBalance(userId, currency);
        String transactionId = generateTransactionId();
        String fullName      = userLookupPort.findFullNameByUserId(userId).orElse("Account Holder");
        String email         = userLookupPort.findEmailByUserId(userId).orElse(null);
        String reference     = generateReference(request.getDepositSystem(), userId);
        String now           = Instant.now().toString();

        historyPort.recordDepositCompleted(
                userId, walletId,
                reference, null,
                transactionId, channel,
                request.getAmount(), BigDecimal.ZERO,
                request.getAmount(), previousBalance,
                newBalance, currency,
                currencySymbol, fullName.toUpperCase(),
                request.getIpAddress(), request.getDeviceId(),
                request.getUserAgent(), request.getGeoLocation(),
                LocalDateTime.now());

        CompletableFuture.runAsync(() -> {
            try {
                notificationPublisher.publishDepositNotification(
                        email, fullName,
                        request.getAmount(), request.getAmount(),
                        fullName.toUpperCase(), newBalance,
                        previousBalance, transactionId, currencySymbol);
            } catch (Exception ex) {
                log.warn("[Deposit] Notification failed txn={}: {}", transactionId, ex.getMessage());
            }
        });

        log.info("[Deposit] Success: userId={} channel={} currency={} amount={} txn={}",
                userId, channel, currency, request.getAmount(), transactionId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status",          "success");
        response.put("transactionId",   transactionId);
        response.put("reference",       reference);
        response.put("previousBalance", previousBalance);
        response.put("newBalance",      newBalance);
        response.put("amount",          request.getAmount());
        response.put("currency",        currency);
        response.put("currencySymbol",  currencySymbol);
        response.put("depositMethod",   channel);
        response.put("accountHolder",   fullName.toUpperCase());
        response.put("timestamp",       now);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private DepositDTO processVerificationResult(Long userId, String reference,
                                                  String channel, DepositVerificationResult result) {
        if (result.isSuccess()) {
            String currency = result.getCurrency();
            Long walletId = walletPort.getWalletId(userId);
            String currencySymbol = walletPort.getCurrencySymbol(currency);
            BigDecimal previousBalance = walletPort.getBalance(userId, currency);

            walletPort.creditWallet(userId, currency, result.getAmount(), reference);

            BigDecimal newBalance = walletPort.getBalance(userId, currency);
            String transactionId = generateTransactionId();
            String fullName = userLookupPort.findFullNameByUserId(userId).orElse("Account Holder");
            String email    = userLookupPort.findEmailByUserId(userId).orElse(null);
            LocalDateTime completedAt = LocalDateTime.now();

            historyPort.recordDepositCompleted(
                    userId, walletId,
                    reference, result.getGatewayReference(),
                    transactionId, channel != null ? channel : "GATEWAY",
                    result.getAmount(), BigDecimal.ZERO,
                    result.getAmount(), previousBalance,
                    newBalance, currency,
                    currencySymbol, fullName.toUpperCase(),
                    null, null, null, null,
                    completedAt);

            CompletableFuture.runAsync(() -> {
                try {
                    notificationPublisher.publishDepositNotification(
                            email, fullName,
                            result.getAmount(), result.getAmount(),
                            fullName.toUpperCase(), newBalance,
                            previousBalance, transactionId, currencySymbol);
                } catch (Exception ex) {
                    log.warn("[Deposit] Notification failed ref={}: {}", reference, ex.getMessage());
                }
            });

            log.info("[Deposit] Completed via gateway: userId={} ref={} amount={}",
                    userId, reference, result.getAmount());

            return DepositDTO.builder()
                    .reference(reference)
                    .amount(result.getAmount())
                    .currency(currency)
                    .status(DepositStatus.COMPLETED)
                    .completedAt(completedAt)
                    .build();

        } else {
            historyPort.recordDepositFailed(userId, reference,
                    result.getAmount() != null ? result.getAmount() : BigDecimal.ZERO,
                    result.getCurrency(), result.getFailureReason());

            log.warn("[Deposit] Failed: userId={} ref={} reason={}", userId, reference, result.getFailureReason());

            return DepositDTO.builder()
                    .reference(reference)
                    .amount(result.getAmount())
                    .currency(result.getCurrency())
                    .status(DepositStatus.FAILED)
                    .build();
        }
    }

    private boolean creditWallet(Long userId, String currency, BigDecimal amount, String reference) {
        try {
            walletPort.creditWallet(userId, currency, amount, reference);
            return true;
        } catch (Exception e) {
            log.error("[Deposit] creditWallet failed userId={} currency={}: {}", userId, currency, e.getMessage());
            return false;
        }
    }

    private DepositVerificationResult callGatewayVerify(DepositGateway gateway, String reference) {
        try {
            return gateway.verify(reference);
        } catch (Exception e) {
            log.error("[Deposit] Gateway verify failed ref={}: {}", reference, e.getMessage());
            throw new DepositException("Deposit verification failed. Please try again.",
                    ErrorCode.DEPOSIT_VERIFICATION_FAILED);
        }
    }

    private DepositGateway resolveGatewayFromReference(String reference) {
        if (reference.startsWith("DEP_PAY_") || reference.startsWith("DEP_CARD_") || reference.startsWith("DEP_USSD_"))
            return gatewayFactory.getGateway(com.epay.domain.deposit.enums.DepositChannel.PAYSTACK);
        if (reference.startsWith("DEP_FLW_"))
            return gatewayFactory.getGateway(com.epay.domain.deposit.enums.DepositChannel.FLUTTERWAVE);
        return gatewayFactory.getGateway(com.epay.domain.deposit.enums.DepositChannel.PAYSTACK);
    }

    private DepositGateway resolveGatewayFromChannel(String channel) {
        return switch (channel.toUpperCase()) {
            case "FLUTTERWAVE" -> gatewayFactory.getGateway(com.epay.domain.deposit.enums.DepositChannel.FLUTTERWAVE);
            default            -> gatewayFactory.getGateway(com.epay.domain.deposit.enums.DepositChannel.PAYSTACK);
        };
    }


    private String generateReference(DepositAndWithdrawSystem system, Long userId) {
        String channelPrefix = switch (system) {
            case PAYSTACK    -> "PAY";
            case FLUTTERWAVE -> "FLW";
            case CARD        -> "CARD";
            case USSD        -> "USSD";
            default          -> "GEN";
        };
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return "DEP_" + channelPrefix + "_" + userId + "_" + random;
    }

    private Long extractUserIdFromReference(String reference) {
        try {
            String[] parts = reference.split("_");
            if (parts.length >= 4) return Long.parseLong(parts[2]);
        } catch (NumberFormatException ignored) {}
        return null;
    }

    private String generateTransactionId() {
        long hash = Math.abs(UUID.randomUUID().getMostSignificantBits());
        return TX_PREFIX + String.valueOf(hash).substring(0, 9);
    }

    private ResponseEntity<Map<String, Object>> error(String message, HttpStatus status, String detail) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("status",  status.value());
        body.put("message", message);
        body.put("detail",  detail);
        return ResponseEntity.status(status).body(body);
    }
}
