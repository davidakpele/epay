package com.epay.withdraw.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.epay.common.events.withdraw.UserTransactionsAgent;
import com.epay.common.interfaces.IBlacklistPort;
import com.epay.common.interfaces.IHistoryPort;
import com.epay.common.interfaces.IIdempotencyPort;
import com.epay.common.interfaces.IWalletNotificationPublisher;
import com.epay.common.interfaces.IWithdrawWalletPort;
import com.epay.common.interfaces.UserLookupPort;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.repository.UserRepository;
import com.epay.domain.common.exception.ErrorHandler;
import com.epay.domain.withdraw.dto.WithdrawalDTO;
import com.epay.domain.withdraw.enums.WithdrawalStatus;
import com.epay.domain.withdraw.enums.WithdrawalType;
import com.epay.domain.withdraw.input.BankWithdrawRequest;
import com.epay.domain.withdraw.input.InternalWithdrawRequest;
import com.epay.withdraw.gateway.PayoutGateway;
import com.epay.withdraw.gateway.PayoutGatewayFactory;
import com.epay.withdraw.gateway.PayoutResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawService {

    private static final String TX_PREFIX    = "NX";
    private static final long   IDEM_TTL_SEC = 86_400L;  // 24 hours

    private final IWithdrawWalletPort          walletPort;
    private final IBlacklistPort               blacklistPort;
    private final IHistoryPort                 historyPort;
    private final IWalletNotificationPublisher notificationPublisher;
    private final UserLookupPort               userLookupPort;
    private final IIdempotencyPort             idempotencyPort;   // Redis-backed
    private final UserRepository               userRepository;
    private final UserTransactionsAgent        transactionsAgent;
    private final PayoutGatewayFactory         gatewayFactory;
    private final ErrorHandler                 errorHandler;

    // =========================================================================
    // Internal transfer (wallet-to-wallet between registered users)
    // =========================================================================

    public ResponseEntity<?> internalWithdrawProcess(InternalWithdrawRequest request) {
        Long userId = request.getUserId();

        // ── Initiator checks ─────────────────────────────────────────────────
        Optional<User> initiatorOpt = userRepository.findByUsername(request.getUsername());
        if (initiatorOpt.isEmpty())
            return errorHandler.error("User not found or account inactive", HttpStatus.NOT_FOUND,
                    "The account does not exist or is inactive.");

        User initiator = initiatorOpt.get();

        if (initiator.isAccountLocked())
            return errorHandler.error("Account is locked", HttpStatus.FORBIDDEN,
                    "This account has been locked. Please contact support.");

        if (!userLookupPort.existsActiveUser(userId))
            return errorHandler.error("User not found or account inactive", HttpStatus.NOT_FOUND,
                    "The account does not exist or is inactive.");

        // ── Recipient checks ─────────────────────────────────────────────────
        Optional<User> recipientOpt = userRepository.findByUsername(request.getRecipient());
        if (recipientOpt.isEmpty())
            return errorHandler.error(
                    "Recipient '" + request.getRecipient() + "' not found", HttpStatus.NOT_FOUND,
                    "The recipient account does not exist or is inactive.");

        User recipient = recipientOpt.get();

        // Prevent self-transfer
        if (initiator.getId().equals(recipient.getId()))
            return errorHandler.error("Self-transfer not allowed", HttpStatus.BAD_REQUEST,
                    "The sender and recipient accounts are the same.");

        // ── Blacklist / fraud checks ──────────────────────────────────────────
        if (blacklistPort.isAccountBlacklisted(userId))
            return errorHandler.error("Account is blacklisted", HttpStatus.FORBIDDEN,
                    "This account has been flagged. Please contact support.");

        // ── Redis idempotency ─────────────────────────────────────────────────
        String idemKey = "withdraw:internal:" + userId + ":" + request.getIdempotencyKey();
        if (idempotencyPort.exists(idemKey))
            return errorHandler.error("Duplicate request", HttpStatus.CONFLICT,
                    "A transfer with this idempotency key has already been processed.");

        // ── Wallet checks ─────────────────────────────────────────────────────
        if (!walletPort.walletExists(userId))
            return errorHandler.error("Wallet not found", HttpStatus.NOT_FOUND,
                    "No wallet found for your account.");

        if (!walletPort.walletExists(recipient.getId()))
            return errorHandler.error("Recipient wallet not found", HttpStatus.NOT_FOUND,
                    "No wallet found for the recipient.");

        String currency = request.getCurrency().toUpperCase();
        if (!walletPort.isCurrencySupported(currency))
            return errorHandler.error("Unsupported currency: " + currency, HttpStatus.BAD_REQUEST,
                    "This currency is not supported.");

        BigDecimal fee            = calculateFee(request.getAmount(), request.getWithdrawalType());
        BigDecimal totalDebit     = request.getAmount().add(fee);
        BigDecimal previousBalance = walletPort.getBalance(userId, currency);

        if (previousBalance.compareTo(totalDebit) < 0)
            return errorHandler.error("Insufficient balance", HttpStatus.BAD_REQUEST,
                    String.format("Available: %s %.2f  Required: %s %.2f (fee: %.2f)",
                            currency, previousBalance, currency, totalDebit, fee));

        // ── PIN verification ──────────────────────────────────────────────────
        if (!walletPort.verifyPin(userId, request.getTransferPin()))
            return errorHandler.error("Invalid transaction PIN", HttpStatus.UNAUTHORIZED,
                    "The transaction PIN you entered is incorrect.");

        // ── Fraud agent checks ────────────────────────────────────────────────
        Long   walletId  = walletPort.getWalletId(userId);
        String fullName  = userLookupPort.findFullNameByUserId(userId).orElse("Account Holder");
        String email     = userLookupPort.findEmailByUserId(userId).orElse(null);
        String firstName = fullName.contains(" ") ? fullName.split(" ")[0] : fullName;
        String lastName  = fullName.contains(" ") ? fullName.split(" ", 2)[1] : "";

        if (transactionsAgent.isHighVolumeOrFrequentTransactions(
                userId, email, firstName, lastName, walletId))
            return errorHandler.error("Transaction blocked", HttpStatus.FORBIDDEN,
                    "Suspicious high-volume activity detected. Wallet temporarily blocked.");

        if (transactionsAgent.isNewAccountAndHighRisk(request.getUsername()))
            return errorHandler.error("Transaction blocked", HttpStatus.FORBIDDEN,
                    "New accounts cannot perform withdrawals immediately after registration.");

        if (transactionsAgent.isFraudulentBehavior(userId, email, firstName, lastName, walletId))
            return errorHandler.error("Transaction blocked", HttpStatus.FORBIDDEN,
                    "Fraudulent activity pattern detected. Account suspended pending review.");

        String reference     = generateReference(request.getWithdrawalType(), userId);
        String transactionId = generateTxnId();
        String currencySymbol = walletPort.getCurrencySymbol(currency);

        try {
            walletPort.debitWallet(userId, currency, totalDebit, reference);
        } catch (Exception e) {
            log.error("[Withdraw.Internal] Debit failed userId={} ref={}: {}", userId, reference, e.getMessage());
            return errorHandler.error("Failed to process withdrawal. Please try again.",
                    HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        BigDecimal newBalance = walletPort.getBalance(userId, currency);

        // Store idemKey now to block duplicate in-flight requests.
        // It is removed once the transaction settles (success or failure below).
        idempotencyPort.store(idemKey, IDEM_TTL_SEC);

        // ── Async: history + notification ─────────────────────────────────────
        String recipientName = userLookupPort.findFullNameByUserId(recipient.getId())
                .orElse(request.getRecipient());
        Long recipientWalletId = walletPort.getWalletId(recipient.getId());

        final BigDecimal finalPreviousBalance = previousBalance;
        final BigDecimal finalNewBalance      = newBalance;

        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(
                        userId, walletId, transactionId, reference,
                        "TRANSFER_DEBIT", "DEBIT",
                        "INTERNAL", WithdrawalStatus.COMPLETED.name(),
                        request.getAmount(), fee, request.getAmount().subtract(fee),
                        finalPreviousBalance, finalNewBalance, currency, currencySymbol,
                        fullName.toUpperCase(),
                        "INTERNAL TRANSFER TO " + recipientName.toUpperCase(),
                        recipientName.toUpperCase(), recipient.getId(), recipientWalletId,
                        null, null, null,
                        "Internal withdrawal via " + request.getWithdrawalType().name(),
                        LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("[Withdraw.Internal] History failed txn={}: {}", transactionId, ex.getMessage());
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                notificationPublisher.publishDebitNotification(
                        email, fee, request.getAmount(),
                        fullName, recipientName,
                        finalNewBalance, currency, transactionId, finalPreviousBalance);
            } catch (Exception ex) {
                log.warn("[Withdraw.Internal] Notification failed txn={}: {}", transactionId, ex.getMessage());
            }
        });

        // Transaction settled — remove idemKey so the client can retry if needed
        idempotencyPort.remove(idemKey);

        log.info("[Withdraw.Internal] userId={} ref={} amount={} {} status=COMPLETED",
                userId, reference, request.getAmount(), currency);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(
                transactionId, reference, previousBalance, newBalance,
                request.getAmount(), fee, currency, currencySymbol,
                WithdrawalType.INTERNAL, WithdrawalStatus.COMPLETED,
                fullName, null, null, null, request.getNarration()));
    }

    // =========================================================================
    // Bank transfer (external payout via gateway)
    // =========================================================================

    public ResponseEntity<?> bankWithdrawProcess(BankWithdrawRequest request) {
        Long userId = request.getUserId();

        // ── User checks ───────────────────────────────────────────────────────
        if (!userLookupPort.existsActiveUser(userId))
            return errorHandler.error("User not found or account inactive", HttpStatus.NOT_FOUND,
                    "The account does not exist or is inactive.");

        if (blacklistPort.isAccountBlacklisted(userId))
            return errorHandler.error("Account is blacklisted", HttpStatus.FORBIDDEN,
                    "This account has been flagged. Please contact support.");

        if (request.getAccountNumber() != null
                && blacklistPort.isAccountNumberBlacklisted(request.getAccountNumber()))
            return errorHandler.error("Recipient account is blacklisted", HttpStatus.FORBIDDEN,
                    "The destination account has been flagged. Please contact support.");

        // ── Redis idempotency ─────────────────────────────────────────────────
        String idemKey = "withdraw:bank:" + userId + ":" + request.getIdempotencyKey();
        if (idempotencyPort.exists(idemKey))
            return errorHandler.error("Duplicate request", HttpStatus.CONFLICT,
                    "A withdrawal with this idempotency key has already been processed.");

        // ── Wallet checks ─────────────────────────────────────────────────────
        if (!walletPort.walletExists(userId))
            return errorHandler.error("Wallet not found", HttpStatus.NOT_FOUND,
                    "No wallet found for this account.");

        String currency = request.getCurrency().toUpperCase();
        if (!walletPort.isCurrencySupported(currency))
            return errorHandler.error("Unsupported currency: " + currency, HttpStatus.BAD_REQUEST,
                    "This currency is not supported.");

        BigDecimal fee            = calculateFee(request.getAmount(), request.getWithdrawalType());
        BigDecimal totalDebit     = request.getAmount().add(fee);
        BigDecimal previousBalance = walletPort.getBalance(userId, currency);

        if (previousBalance.compareTo(totalDebit) < 0)
            return errorHandler.error("Insufficient balance", HttpStatus.BAD_REQUEST,
                    String.format("Available: %s %.2f  Required: %s %.2f (fee: %.2f)",
                            currency, previousBalance, currency, totalDebit, fee));

        // ── PIN verification ──────────────────────────────────────────────────
        if (!walletPort.verifyPin(userId, request.getTransferPin()))
            return errorHandler.error("Invalid transaction PIN", HttpStatus.UNAUTHORIZED,
                    "The transaction PIN you entered is incorrect.");

        // ── Fraud agent checks ────────────────────────────────────────────────
        Long   walletId  = walletPort.getWalletId(userId);
        String fullName  = userLookupPort.findFullNameByUserId(userId).orElse("Account Holder");
        String email     = userLookupPort.findEmailByUserId(userId).orElse(null);
        String firstName = fullName.contains(" ") ? fullName.split(" ")[0] : fullName;
        String lastName  = fullName.contains(" ") ? fullName.split(" ", 2)[1] : "";

        if (transactionsAgent.isHighVolumeOrFrequentTransactions(
                userId, email, firstName, lastName, walletId))
            return errorHandler.error("Transaction blocked", HttpStatus.FORBIDDEN,
                    "Suspicious high-volume activity detected. Wallet temporarily blocked.");

        if (transactionsAgent.isFraudulentBehavior(userId, email, firstName, lastName, walletId))
            return errorHandler.error("Transaction blocked", HttpStatus.FORBIDDEN,
                    "Fraudulent activity pattern detected. Account suspended pending review.");

        // ── Execute debit ─────────────────────────────────────────────────────
        String reference     = generateReference(request.getWithdrawalType(), userId);
        String transactionId = generateTxnId();
        String currencySymbol = walletPort.getCurrencySymbol(currency);

        try {
            walletPort.debitWallet(userId, currency, totalDebit, reference);
        } catch (Exception e) {
            log.error("[Withdraw.Bank] Debit failed userId={} ref={}: {}", userId, reference, e.getMessage());
            idempotencyPort.remove(idemKey);  // debit failed — free the key so client can retry
            return errorHandler.error("Failed to process withdrawal. Please try again.",
                    HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        BigDecimal newBalance = walletPort.getBalance(userId, currency);

        // Store idemKey to block duplicate in-flight requests while gateway processes.
        // Removed below once the transaction fully settles.
        idempotencyPort.store(idemKey, IDEM_TTL_SEC);

        // ── Gateway payout ────────────────────────────────────────────────────
        WithdrawalStatus status = WithdrawalStatus.COMPLETED;
        String failureReason   = null;

        try {
            PayoutGateway gateway = gatewayFactory.getGateway(request.getWithdrawalType());
            PayoutResult result   = gateway.payout(reference,
                    request.getAccountNumber(), request.getBankCode(),
                    request.getAccountName(), request.getAmount(),
                    currency, request.getNarration());

            if (!result.isSuccess()) {
                walletPort.refundWallet(userId, currency, totalDebit, reference + "_REFUND");
                idempotencyPort.remove(idemKey);  // failed — allow client to retry
                status        = WithdrawalStatus.FAILED;
                failureReason = result.getFailureReason();
                log.warn("[Withdraw.Bank] Payout failed ref={} reason={}", reference, failureReason);
            }
        } catch (Exception e) {
            walletPort.refundWallet(userId, currency, totalDebit, reference + "_REFUND");
            idempotencyPort.remove(idemKey);  // exception — allow client to retry
            status        = WithdrawalStatus.FAILED;
            failureReason = "Gateway error: " + e.getMessage();
            log.error("[Withdraw.Bank] Gateway exception ref={}: {}", reference, e.getMessage());
        }

        // ── Async: history + notification ─────────────────────────────────────
        final WithdrawalStatus finalStatus      = status;
        final BigDecimal finalPreviousBalance   = previousBalance;
        final BigDecimal finalNewBalance        = newBalance;
        final String finalFailureReason         = failureReason;

        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(
                        userId, walletId, transactionId, reference,
                        "WITHDRAWAL", "DEBIT",
                        request.getWithdrawalType().name(), finalStatus.name(),
                        request.getAmount(), fee, request.getAmount().subtract(fee),
                        finalPreviousBalance, finalNewBalance, currency, currencySymbol,
                        fullName.toUpperCase(),
                        "BANK WITHDRAWAL TO " + request.getAccountName().toUpperCase()
                                + " (" + request.getAccountNumber() + ")",
                        request.getAccountName(), null, null,
                        null, null, null,
                        finalStatus == WithdrawalStatus.FAILED ? finalFailureReason
                                : "Bank withdrawal via " + request.getWithdrawalType().name(),
                        finalStatus == WithdrawalStatus.COMPLETED ? LocalDateTime.now() : null);
            } catch (Exception ex) {
                log.warn("[Withdraw.Bank] History failed txn={}: {}", transactionId, ex.getMessage());
            }
        });

        if (finalStatus == WithdrawalStatus.COMPLETED) {
            CompletableFuture.runAsync(() -> {
                try {
                    notificationPublisher.publishDebitNotification(
                            email, fee, request.getAmount(),
                            fullName, request.getAccountName(),
                            finalNewBalance, currency, transactionId, finalPreviousBalance);
                } catch (Exception ex) {
                    log.warn("[Withdraw.Bank] Notification failed txn={}: {}", transactionId, ex.getMessage());
                }
            });
        }

        log.info("[Withdraw.Bank] userId={} ref={} amount={} {} status={}",
                userId, reference, request.getAmount(), currency, status);

        // Transaction settled — remove idemKey whether success or failure
        idempotencyPort.remove(idemKey);

        if (status == WithdrawalStatus.FAILED)
            return errorHandler.error("Withdrawal failed: " + failureReason,
                    HttpStatus.BAD_GATEWAY, failureReason);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(
                transactionId, reference, previousBalance, newBalance,
                request.getAmount(), fee, currency, currencySymbol,
                request.getWithdrawalType(), WithdrawalStatus.COMPLETED,
                fullName, request.getBankCode(), request.getAccountNumber(),
                request.getAccountName(), request.getNarration()));
    }

    // =========================================================================
    // Shared helpers
    // =========================================================================

    @SuppressWarnings("unused") // fee logic to be implemented — amount and type reserved
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

    private Map<String, Object> buildResponse(
            String transactionId, String reference,
            BigDecimal previousBalance, BigDecimal newBalance,
            BigDecimal amount, BigDecimal fee,
            String currency, String currencySymbol,
            WithdrawalType type, WithdrawalStatus status,
            String accountHolder, String bankCode,
            String accountNumber, String accountName, String narration) {

        WithdrawalDTO dto = WithdrawalDTO.builder()
                .transactionId(transactionId)
                .reference(reference)
                .amount(amount)
                .fee(fee)
                .netAmount(amount.subtract(fee))
                .currency(currency)
                .currencySymbol(currencySymbol)
                .withdrawalType(type)
                .status(status)
                .bankCode(bankCode)
                .accountNumber(accountNumber)
                .accountName(accountName)
                .narration(narration)
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
        response.put("amount",          amount);
        response.put("fee",             fee);
        response.put("currency",        currency);
        response.put("currencySymbol",  currencySymbol);
        response.put("withdrawalType",  type.name());
        response.put("status",          status.name());
        response.put("accountHolder",   accountHolder.toUpperCase());
        response.put("timestamp",       java.time.Instant.now().toString());
        response.put("data",            dto);
        return response;
    }
}
