package com.epay.deposit.service;

import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ConflictException;
import com.epay.common.exception.DepositException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.interfaces.IDepositHistoryPort;
import com.epay.common.interfaces.IDepositWalletPort;
import com.epay.common.interfaces.IWalletNotificationPublisher;
import com.epay.common.interfaces.UserLookupPort;
import com.epay.deposit.gateway.DepositGateway;
import com.epay.deposit.gateway.DepositGatewayFactory;
import com.epay.deposit.gateway.DepositVerificationResult;
import com.epay.domain.deposit.dto.DepositDTO;
import com.epay.domain.deposit.enums.DepositStatus;
import com.epay.domain.deposit.input.InitiateDepositRequest;
import com.epay.domain.deposit.input.VerifyDepositRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Deposit service — no entity, no database table.
 *
 * Flow:
 *   1. initiate()  → calls gateway, returns payment URL, writes INITIATED to history
 *   2. verify()    → calls gateway, credits wallet, writes COMPLETED/FAILED to history,
 *                    fires deposit notification
 *   3. handleWebhook() → same as verify() but triggered by gateway callback
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositGatewayFactory      gatewayFactory;
    private final IDepositWalletPort         walletPort;
    private final IDepositHistoryPort        historyPort;
    private final IWalletNotificationPublisher notificationPublisher;
    private final UserLookupPort             userLookupPort;

    // -------------------------------------------------------------------------
    // Initiate
    // -------------------------------------------------------------------------

    public DepositDTO initiate(Long userId, InitiateDepositRequest request) {

        if (!userLookupPort.existsActiveUser(userId))
            throw new ResourceNotFoundException("User not found or inactive");

        String email = userLookupPort.findEmailByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User email not found"));

        String reference = generateReference();

        DepositGateway gateway = gatewayFactory.getGateway(request.getChannel());
        String paymentUrl;
        try {
            paymentUrl = gateway.initiate(reference, email, request.getAmount(),
                    request.getCurrency(), request.getCallbackUrl());
        } catch (Exception e) {
            log.error("Gateway initiation failed for userId={} ref={}: {}", userId, reference, e.getMessage());
            throw new DepositException("Payment gateway error. Please try again.",
                    ErrorCode.DEPOSIT_GATEWAY_ERROR);
        }

        // Write initiated event to history
        historyPort.recordDepositInitiated(userId, reference, request.getAmount(),
                request.getCurrency(), request.getChannel().name());

        log.info("Deposit initiated: userId={} ref={} amount={} currency={}",
                userId, reference, request.getAmount(), request.getCurrency());

        return DepositDTO.builder()
                .reference(reference)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .channel(request.getChannel())
                .status(DepositStatus.PENDING)
                .paymentUrl(paymentUrl)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // Verify (user redirect / polling)
    // -------------------------------------------------------------------------

    public DepositDTO verify(Long userId, VerifyDepositRequest request) {
        if (!userLookupPort.existsActiveUser(userId))
            throw new ResourceNotFoundException("User not found or inactive");

        // Reference encodes the channel — first 3 chars: PAY=Paystack, FLW=Flutterwave
        DepositGateway gateway = resolveGatewayFromReference(request.getReference());
        DepositVerificationResult result = verifyWithGateway(gateway, request.getReference());

        return buildResultAndNotify(userId, request.getReference(), null, result);
    }

    // -------------------------------------------------------------------------
    // Webhook handler
    // -------------------------------------------------------------------------

    public void handleWebhook(String signature, String rawPayload, String reference, String channel) {
        DepositGateway gateway = resolveGatewayFromChannel(channel);

        if (!gateway.validateWebhookSignature(rawPayload, signature)) {
            log.warn("[Webhook] Invalid signature for ref={}", reference);
            throw new BadRequestException("Invalid webhook signature", ErrorCode.INVALID_INPUT);
        }

        DepositVerificationResult result = verifyWithGateway(gateway, reference);

        // Resolve userId from reference — encoded in history or passed via payload
        // For webhook we only notify; wallet credit is idempotent via gateway check
        log.info("[Webhook] Processing deposit ref={} success={}", reference, result.isSuccess());

        if (result.isSuccess()) {
            // Credit is handled — fire notification only (history written in verify step)
            fireDepositNotification(null, result.getAmount(), result.getCurrency(),
                    reference, result.getGatewayReference());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private DepositVerificationResult verifyWithGateway(DepositGateway gateway, String reference) {
        try {
            return gateway.verify(reference);
        } catch (Exception e) {
            log.error("Gateway verification failed for ref={}: {}", reference, e.getMessage());
            throw new DepositException("Deposit verification failed. Please try again.",
                    ErrorCode.DEPOSIT_VERIFICATION_FAILED);
        }
    }

    private DepositDTO buildResultAndNotify(Long userId, String reference,
                                             String channel, DepositVerificationResult result) {
        DepositStatus status;
        LocalDateTime completedAt = null;

        if (result.isSuccess()) {
            // Credit wallet
            walletPort.creditWallet(userId, result.getCurrency(),
                    result.getAmount(), reference);

            // Write completed to history
            completedAt = LocalDateTime.now();
            historyPort.recordDepositCompleted(userId, reference, result.getAmount(),
                    result.getCurrency(), result.getGatewayReference(), completedAt);

            status = DepositStatus.COMPLETED;

            // Async notification
            fireDepositNotification(userId, result.getAmount(), result.getCurrency(),
                    reference, result.getGatewayReference());

            log.info("Deposit completed: userId={} ref={} amount={}",
                    userId, reference, result.getAmount());
        } else {
            historyPort.recordDepositFailed(userId, reference,
                    result.getAmount() != null ? result.getAmount() : BigDecimal.ZERO,
                    result.getCurrency(), result.getFailureReason());

            status = DepositStatus.FAILED;

            log.warn("Deposit failed: userId={} ref={} reason={}", userId, reference, result.getFailureReason());
        }

        return DepositDTO.builder()
                .reference(reference)
                .amount(result.getAmount())
                .currency(result.getCurrency())
                .status(status)
                .completedAt(completedAt)
                .build();
    }

    private void fireDepositNotification(Long userId, BigDecimal amount, String currency,
                                          String reference, String gatewayRef) {
        String email    = userId != null ? userLookupPort.findEmailByUserId(userId).orElse(null) : null;
        String fullName = userId != null ? userLookupPort.findFullNameByUserId(userId).orElse(null) : null;

        CompletableFuture.runAsync(() -> {
            try {
                notificationPublisher.publishDepositNotification(
                        email, fullName, amount, amount,
                        fullName, amount, null, reference, currency);
            } catch (Exception ex) {
                log.warn("[Deposit] Notification failed ref={}: {}", reference, ex.getMessage());
            }
        });
    }

    private DepositGateway resolveGatewayFromReference(String reference) {
        // Reference prefix convention: DEP_PAY_... = Paystack, DEP_FLW_... = Flutterwave
        if (reference.contains("PAY"))
            return gatewayFactory.getGateway(com.epay.domain.deposit.enums.DepositChannel.PAYSTACK);
        if (reference.contains("FLW"))
            return gatewayFactory.getGateway(com.epay.domain.deposit.enums.DepositChannel.FLUTTERWAVE);
        // Default to Paystack for legacy references
        return gatewayFactory.getGateway(com.epay.domain.deposit.enums.DepositChannel.PAYSTACK);
    }

    private DepositGateway resolveGatewayFromChannel(String channel) {
        return gatewayFactory.getGateway(
                com.epay.domain.deposit.enums.DepositChannel.valueOf(channel.toUpperCase()));
    }

    private String generateReference() {
        return "DEP_PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
