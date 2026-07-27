package com.epay.withdraw.service;

import com.epay.common.interfaces.IBlacklistPort;
import com.epay.common.interfaces.IHistoryPort;
import com.epay.common.interfaces.IIdempotencyPort;
import com.epay.common.interfaces.IWalletNotificationPublisher;
import com.epay.common.interfaces.IWithdrawWalletPort;
import com.epay.common.interfaces.UserLookupPort;
import com.epay.domain.withdraw.dto.WithdrawalDTO;
import com.epay.domain.withdraw.enums.WithdrawalStatus;
import com.epay.domain.withdraw.enums.WithdrawalType;
import com.epay.domain.withdraw.input.WithdrawRequest;
import com.epay.withdraw.gateway.PayoutGateway;
import com.epay.withdraw.gateway.PayoutGatewayFactory;
import com.epay.withdraw.gateway.PayoutResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawService {

    private static final String TX_PREFIX    = "NX";
    private static final long   IDEM_TTL_SEC = 86_400L; 

    private final IWithdrawWalletPort          walletPort;
    private final IBlacklistPort               blacklistPort;
    private final IHistoryPort                 historyPort;
    private final IWalletNotificationPublisher notificationPublisher;
    private final UserLookupPort               userLookupPort;
    private final IIdempotencyPort             idempotencyPort;
    private final PayoutGatewayFactory         gatewayFactory;

    public ResponseEntity<?> withdraw(Long userId, WithdrawRequest request) {

        if (!userLookupPort.existsActiveUser(userId))
            return error("User not found or account inactive", HttpStatus.NOT_FOUND,
                    "The account does not exist or is inactive.");

        if (blacklistPort.isAccountBlacklisted(userId))
            return error("Account is blacklisted", HttpStatus.FORBIDDEN,
                    "This account has been flagged. Please contact support.");

        if (request.getIpAddress() != null && blacklistPort.isIpBlacklisted(request.getIpAddress()))
            return error("Request blocked", HttpStatus.FORBIDDEN,
                    "This request has been blocked for security reasons.");

        if (request.getAccountNumber() != null
                && blacklistPort.isAccountNumberBlacklisted(request.getAccountNumber()))
            return error("Recipient account is blacklisted", HttpStatus.FORBIDDEN,
                    "The destination account has been flagged. Please contact support.");

        String idemKey = "withdraw:" + userId + ":" + request.getIdempotencyKey();
        if (idempotencyPort.exists(idemKey))
            return error("Duplicate request", HttpStatus.CONFLICT,
                    "A withdrawal with this idempotency key has already been processed.");

        if (!walletPort.walletExists(userId))
            return error("Wallet not found", HttpStatus.NOT_FOUND,
                    "No wallet found for this account.");

        String currency = request.getCurrency().toUpperCase();
        if (!walletPort.isCurrencySupported(currency))
            return error("Unsupported currency: " + currency, HttpStatus.BAD_REQUEST,
                    "This currency is not supported.");
        BigDecimal fee            = calculateFee(request.getAmount(), request.getWithdrawalType());
        BigDecimal totalDebit     = request.getAmount().add(fee);
        BigDecimal previousBalance = walletPort.getBalance(userId, currency);

        if (previousBalance.compareTo(totalDebit) < 0)
            return error("Insufficient balance", HttpStatus.BAD_REQUEST,
                    String.format("Available: %s %.2f, Required: %s %.2f (inc. fee: %.2f)",
                            currency, previousBalance, currency, totalDebit, fee));

        if (!walletPort.verifyPin(userId, request.getTransactionPin()))
            return error("Invalid transaction PIN", HttpStatus.UNAUTHORIZED,
                    "The transaction PIN you entered is incorrect.");

        String reference     = generateReference(request.getWithdrawalType(), userId);
        String transactionId = generateTxnId();
        String currencySymbol = walletPort.getCurrencySymbol(currency);
        String fullName      = userLookupPort.findFullNameByUserId(userId).orElse("Account Holder");
        String email         = userLookupPort.findEmailByUserId(userId).orElse(null);
        Long   walletId      = walletPort.getWalletId(userId);

        try {
            walletPort.debitWallet(userId, currency, totalDebit, reference);
        } catch (Exception e) {
            log.error("[Withdraw] Wallet debit failed userId={} ref={}: {}", userId, reference, e.getMessage());
            return error("Failed to process withdrawal. Please try again.",
                    HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        BigDecimal newBalance = walletPort.getBalance(userId, currency);

        idempotencyPort.store(idemKey, IDEM_TTL_SEC);

        WithdrawalStatus status = WithdrawalStatus.COMPLETED;
        String failureReason    = null;

        if (request.getWithdrawalType() == WithdrawalType.BANK_TRANSFER) {
            try {
                PayoutGateway gateway = gatewayFactory.getGateway(request.getWithdrawalType());
                PayoutResult result   = gateway.payout(reference, request.getAccountNumber(),
                        request.getBankCode(), request.getAccountName(),
                        request.getAmount(), currency, request.getNarration());

                if (!result.isSuccess()) {
                    walletPort.refundWallet(userId, currency, totalDebit, reference + "_REFUND");
                    idempotencyPort.remove(idemKey);
                    status        = WithdrawalStatus.FAILED;
                    failureReason = result.getFailureReason();
                    log.warn("[Withdraw] Payout failed ref={} reason={}", reference, failureReason);
                }
            } catch (Exception e) {
                walletPort.refundWallet(userId, currency, totalDebit, reference + "_REFUND");
                idempotencyPort.remove(idemKey);
                status        = WithdrawalStatus.FAILED;
                failureReason = "Gateway error: " + e.getMessage();
                log.error("[Withdraw] Gateway exception ref={}: {}", reference, e.getMessage());
            }
        }

        final WithdrawalStatus finalStatus = status;
        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(
                        userId, walletId, transactionId, reference,
                        "WITHDRAWAL", "DEBIT",
                        request.getWithdrawalType().name(),
                        finalStatus.name(),
                        request.getAmount(), fee, request.getAmount().subtract(fee),
                        previousBalance, newBalance, currency, currencySymbol,
                        fullName.toUpperCase(),
                        "WITHDRAWAL//FROM " + fullName.toUpperCase() + " " + currency + " ACCOUNT",
                        request.getAccountName(), null, null,
                        request.getIpAddress(), request.getDeviceId(), request.getUserAgent(),
                        "Withdrawal via " + request.getWithdrawalType().name(),
                        finalStatus == WithdrawalStatus.COMPLETED ? LocalDateTime.now() : null);
            } catch (Exception ex) {
                log.warn("[Withdraw] History failed txn={}: {}", transactionId, ex.getMessage());
            }
        });

        if (status == WithdrawalStatus.COMPLETED) {
            CompletableFuture.runAsync(() -> {
                try {
                    notificationPublisher.publishDebitNotification(
                            email, fee, request.getAmount(),
                            fullName, request.getAccountName() != null ? request.getAccountName() : "Bank Account",
                            newBalance, currency, transactionId, previousBalance);
                } catch (Exception ex) {
                    log.warn("[Withdraw] Notification failed txn={}: {}", transactionId, ex.getMessage());
                }
            });
        }

        log.info("[Withdraw] {} userId={} ref={} amount={} {} fee={} status={}",
                request.getWithdrawalType(), userId, reference,
                request.getAmount(), currency, fee, status);

        if (status == WithdrawalStatus.FAILED) {
            return error("Withdrawal failed: " + failureReason, HttpStatus.BAD_GATEWAY, failureReason);
        }

        WithdrawalDTO dto = WithdrawalDTO.builder()
                .transactionId(transactionId)
                .reference(reference)
                .amount(request.getAmount())
                .fee(fee)
                .netAmount(request.getAmount().subtract(fee))
                .currency(currency)
                .currencySymbol(currencySymbol)
                .withdrawalType(request.getWithdrawalType())
                .status(status)
                .bankCode(request.getBankCode())
                .accountNumber(request.getAccountNumber())
                .accountName(request.getAccountName())
                .narration(request.getNarration())
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success",         true);
        response.put("transactionId",   transactionId);
        response.put("reference",       reference);
        response.put("previousBalance", previousBalance);
        response.put("newBalance",      newBalance);
        response.put("amount",          request.getAmount());
        response.put("fee",             fee);
        response.put("currency",        currency);
        response.put("currencySymbol",  currencySymbol);
        response.put("withdrawalType",  request.getWithdrawalType().name());
        response.put("status",          status.name());
        response.put("accountHolder",   fullName.toUpperCase());
        response.put("timestamp",       java.time.Instant.now().toString());
        response.put("data",            dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

 
    private BigDecimal calculateFee(BigDecimal amount, WithdrawalType type) {
        return BigDecimal.ZERO;
    }

    private String generateReference(WithdrawalType type, Long userId) {
        String prefix = switch (type) {
            case BANK_TRANSFER -> "WDR_BNK";
            case INTERNAL      -> "WDR_INT";
            case USSD          -> "WDR_USSD";
            case CARD          -> "WDR_CARD";
        };
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return prefix + "_" + userId + "_" + random;
    }

    private String generateTxnId() {
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