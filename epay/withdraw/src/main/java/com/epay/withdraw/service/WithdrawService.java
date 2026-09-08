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
import com.epay.common.config.services.PaystackClient;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.repository.UserRepository;
import com.epay.domain.common.exception.ErrorHandler;
import com.epay.domain.withdraw.dto.WithdrawalDTO;
import com.epay.domain.withdraw.enums.WithdrawalStatus;
import com.epay.domain.withdraw.enums.WithdrawalType;
import com.epay.domain.withdraw.input.BankWithdrawRequest;
import com.epay.domain.withdraw.input.InternalWithdrawRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawService {

    private static final long   IDEM_TTL_SEC = 86_400L; 

    private final IWithdrawWalletPort          walletPort;
    private final IBlacklistPort               blacklistPort;
    private final IHistoryPort                 historyPort;
    private final IWalletNotificationPublisher notificationPublisher;
    private final UserLookupPort               userLookupPort;
    private final IIdempotencyPort             idempotencyPort; 
    private final UserRepository               userRepository;
    private final UserTransactionsAgent        transactionsAgent;
    private final ErrorHandler                 errorHandler;
    private final PaystackClient               paystackClient;


    public ResponseEntity<?> internalWithdrawProcess(InternalWithdrawRequest request) {
        Long userId = request.getUserId();

        Optional<User> initiatorOpt = userRepository.findByUsername(request.getUsername());
        if (initiatorOpt.isEmpty())
            return errorHandler.error("User not found or account inactive", HttpStatus.NOT_FOUND,
                    "The account does not exist or is inactive.");

        User initiator = initiatorOpt.get();

        if (initiator.isAccountLocked())
            return errorHandler.error("Account is locked", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "This account has been locked. Please contact support.");

        if (!userLookupPort.existsActiveUser(userId))
            return errorHandler.error("User not found or account inactive", HttpStatus.NOT_FOUND,
                    "The account does not exist or is inactive.");

        Optional<User> recipientOpt = userRepository.findByUsername(request.getRecipient());
        if (recipientOpt.isEmpty())
            return errorHandler.error(
                    "Recipient '" + request.getRecipient() + "' not found", HttpStatus.NOT_FOUND,
                    "The recipient account does not exist or is inactive.");

        User recipient = recipientOpt.get();

        if (initiator.getId().equals(recipient.getId()))
            return errorHandler.error("Self-transfer not allowed", HttpStatus.BAD_REQUEST,
                    "The sender and recipient accounts are the same.");

        if (blacklistPort.isAccountBlacklisted(userId))
            return errorHandler.error("Account is blacklisted", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "This account has been flagged. Please contact support.");

        String idemKey = "withdraw:internal:" + userId + ":" + request.getIdempotencyKey();
        if (idempotencyPort.exists(idemKey))
            return errorHandler.error("Duplicate request", HttpStatus.CONFLICT,
                    "A transfer with this idempotency key has already been processed.");

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

        if (!walletPort.verifyPin(userId, request.getTransferPin()))
            return errorHandler.error("Invalid transaction PIN", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "The transaction PIN you entered is incorrect.");

        Long   walletId  = walletPort.getWalletId(userId);
        String fullName  = userLookupPort.findFullNameByUserId(userId).orElse("Account Holder");
        String email     = userLookupPort.findEmailByUserId(userId).orElse(null);
        String firstName = fullName.contains(" ") ? fullName.split(" ")[0] : fullName;
        String lastName  = fullName.contains(" ") ? fullName.split(" ", 2)[1] : "";

        if (transactionsAgent.isHighVolumeOrFrequentTransactions(
                userId, email, firstName, lastName, walletId))
            return errorHandler.error("Transaction blocked", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "Suspicious high-volume activity detected. Wallet temporarily blocked.");

        if (transactionsAgent.isNewAccountAndHighRisk(request.getUsername()))
            return errorHandler.error("Transaction blocked", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "New accounts cannot perform withdrawals immediately after registration.");

        if (transactionsAgent.isFraudulentBehavior(userId, email, firstName, lastName, walletId))
            return errorHandler.error("Transaction blocked", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
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

        String creditReference = reference + "_CREDIT";
        try {
            walletPort.creditWallet(recipient.getId(), currency, request.getAmount(), creditReference);
        } catch (Exception e) {
            log.error("[Withdraw.Internal] Credit failed recipientId={} ref={}: {}", recipient.getId(), reference, e.getMessage());
            walletPort.refundWallet(userId, currency, totalDebit, reference + "_REFUND");
            idempotencyPort.remove(idemKey);
            return errorHandler.error("Failed to credit recipient. Transaction reversed.",
                    HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        final BigDecimal finalRecipientNewBalance = walletPort.getBalance(recipient.getId(), currency);
        idempotencyPort.store(idemKey, IDEM_TTL_SEC);

        String recipientName     = userLookupPort.findFullNameByUserId(recipient.getId())
                .orElse(request.getRecipient());
        String recipientEmail    = userLookupPort.findEmailByUserId(recipient.getId()).orElse(null);
        Long   recipientWalletId = walletPort.getWalletId(recipient.getId());

        final BigDecimal finalPreviousBalance     = previousBalance;
        final BigDecimal finalNewBalance          = newBalance;

        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(
                        userId, walletId, transactionId, reference,
                        "TRANSFER_DEBIT", "DEBIT",
                        "INTERNAL", "DELIVERED",
                        request.getAmount(), fee, request.getAmount().subtract(fee),
                        finalPreviousBalance, finalNewBalance, currency, currencySymbol,
                        fullName.toUpperCase(),
                        "INTERNAL TRANSFER TO " + recipientName.toUpperCase(),
                        recipientName.toUpperCase(), recipient.getId(), recipientWalletId,
                        null, null, null,
                        "Internal withdrawal via " + request.getWithdrawalType().name(),
                        LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("[Withdraw.Internal] Debit history failed txn={}: {}", transactionId, ex.getMessage());
            }
        });

        String creditTxnId = generateTxnId();
        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(
                        recipient.getId(), recipientWalletId, creditTxnId, creditReference,
                        "TRANSFER_CREDIT", "CREDIT",
                        "INTERNAL", "DELIVERED",
                        request.getAmount(), BigDecimal.ZERO, request.getAmount(),
                        walletPort.getBalance(recipient.getId(), currency).subtract(request.getAmount()),
                        finalRecipientNewBalance, currency, currencySymbol,
                        recipientName.toUpperCase(),
                        "INTERNAL TRANSFER FROM " + fullName.toUpperCase(),
                        fullName.toUpperCase(), userId, walletId,
                        null, null, null,
                        "Internal credit from " + fullName,
                        LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("[Withdraw.Internal] Credit history failed txn={}: {}", creditTxnId, ex.getMessage());
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                notificationPublisher.publishDebitNotification(
                        email, fee, request.getAmount(),
                        fullName, recipientName,
                        finalNewBalance, currency, transactionId, finalPreviousBalance);
            } catch (Exception ex) {
                log.warn("[Withdraw.Internal] Debit notification failed txn={}: {}", transactionId, ex.getMessage());
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                notificationPublisher.publishCreditNotification(
                        recipientEmail, request.getAmount(),
                        fullName, recipientName,
                        finalRecipientNewBalance, currency,
                        creditTxnId,
                        finalRecipientNewBalance.subtract(request.getAmount()));
            } catch (Exception ex) {
                log.warn("[Withdraw.Internal] Credit notification failed txn={}: {}", creditTxnId, ex.getMessage());
            }
        });

        idempotencyPort.remove(idemKey);

        log.info("[Withdraw.Internal] userId={} ref={} amount={} {} status=COMPLETED",
                userId, reference, request.getAmount(), currency);

        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(
                transactionId, reference, previousBalance, newBalance,
                request.getAmount(), fee, currency, currencySymbol,
                WithdrawalType.INTERNAL, WithdrawalStatus.COMPLETED,
                fullName, null, null, null, request.getNarration()));
    }


    public ResponseEntity<?> bankWithdrawProcess(BankWithdrawRequest request) {
        Long userId = request.getUserId();

        if (!userLookupPort.existsActiveUser(userId))
            return errorHandler.error("User not found or account inactive", HttpStatus.NOT_FOUND,
                    "The account does not exist or is inactive.");

        if (blacklistPort.isAccountBlacklisted(userId))
            return errorHandler.error("Account is blacklisted", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "This account has been flagged. Please contact support.");

        if (request.getAccountNumber() != null
                && blacklistPort.isAccountNumberBlacklisted(request.getAccountNumber()))
            return errorHandler.error("Recipient account is blacklisted", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "The destination account has been flagged. Please contact support.");

        String idemKey = "withdraw:bank:" + userId + ":" + request.getIdempotencyKey();
        if (idempotencyPort.exists(idemKey))
            return errorHandler.error("Duplicate request", HttpStatus.CONFLICT,
                    "A withdrawal with this idempotency key has already been processed.");

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

        if (!walletPort.verifyPin(userId, request.getTransferPin()))
            return errorHandler.error("Invalid transaction PIN", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "The transaction PIN you entered is incorrect.");

        Long   walletId  = walletPort.getWalletId(userId);
        String fullName  = userLookupPort.findFullNameByUserId(userId).orElse("Account Holder");
        String email     = userLookupPort.findEmailByUserId(userId).orElse(null);
        String firstName = fullName.contains(" ") ? fullName.split(" ")[0] : fullName;
        String lastName  = fullName.contains(" ") ? fullName.split(" ", 2)[1] : "";

        if (transactionsAgent.isHighVolumeOrFrequentTransactions(
                userId, email, firstName, lastName, walletId))
            return errorHandler.error("Transaction blocked", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "Suspicious high-volume activity detected. Wallet temporarily blocked.");

        if (transactionsAgent.isFraudulentBehavior(userId, email, firstName, lastName, walletId))
            return errorHandler.error("Transaction blocked", HttpStatus.NETWORK_AUTHENTICATION_REQUIRED,
                    "Fraudulent activity pattern detected. Account suspended pending review.");

        String reference      = generateReference(request.getWithdrawalType(), userId);
        String transactionId  = generateTxnId();
        String currencySymbol = walletPort.getCurrencySymbol(currency);

        Map<String, Object> paystackData;
        try {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("transaction_id",  transactionId);
            metadata.put("user_id",         userId);
            metadata.put("account_name",    request.getAccountName());
            metadata.put("account_number",  request.getAccountNumber());
            metadata.put("bank_code",       request.getBankCode());
            metadata.put("withdrawal_type", request.getWithdrawalType().name());
            metadata.put("narration",       request.getNarration() != null ? request.getNarration() : "Bank withdrawal");

            paystackData = paystackClient.initializeTransaction(
                    reference, email, request.getAmount(), currency, null, metadata);
        } catch (Exception e) {
            idempotencyPort.remove(idemKey);
            log.error("[Withdraw.Bank] Paystack init failed userId={} ref={}: {}", userId, reference, e.getMessage());
            return errorHandler.error("Could not initialize payment. Please try again.",
                    HttpStatus.BAD_GATEWAY, e.getMessage());
        }

        String authorizationUrl  = (String) paystackData.get("authorization_url");
        String accessCode        = (String) paystackData.get("access_code");
        String paystackReference = (String) paystackData.getOrDefault("reference", reference);

        final BigDecimal currentBalance = walletPort.getBalance(userId, currency);
        CompletableFuture.runAsync(() -> {
            try {
                historyPort.record(
                        userId, walletId, transactionId, reference,
                        "WITHDRAWAL", "DEBIT",
                        request.getWithdrawalType().name(), "PENDING",
                        request.getAmount(), fee, request.getAmount().subtract(fee),
                        currentBalance, currentBalance,
                        currency, currencySymbol,
                        fullName.toUpperCase(),
                        "BANK WITHDRAWAL TO " + request.getAccountName().toUpperCase()
                                + " (" + request.getAccountNumber() + ")",
                        request.getAccountName(), null, null,
                        null, null, null,
                        "Awaiting payment confirmation — " + request.getWithdrawalType().name(),
                        null);
            } catch (Exception ex) {
                log.warn("[Withdraw.Bank] History (PENDING) failed txn={}: {}", transactionId, ex.getMessage());
            }
        });

        idempotencyPort.store(idemKey, IDEM_TTL_SEC);

        log.info("[Withdraw.Bank] userId={} ref={} paystackRef={} amount={} {} status=PENDING_PAYMENT",
                userId, reference, paystackReference, request.getAmount(), currency);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(buildBankInitResponse(
                        transactionId, reference, paystackReference,
                        currentBalance, request.getAmount(), fee,
                        currency, currencySymbol, request.getWithdrawalType(),
                        fullName, request.getBankCode(),
                        request.getAccountNumber(), request.getAccountName(),
                        request.getNarration(), authorizationUrl, accessCode,
                        email));
    }

    private Map<String, Object> buildBankInitResponse(
            String transactionId, String reference, String paystackReference,
            BigDecimal currentBalance, BigDecimal amount, BigDecimal fee,
            String currency, String currencySymbol, WithdrawalType type,
            String accountHolder, String bankCode, String accountNumber,
            String accountName, String narration,
            String authorizationUrl, String accessCode, String email) {

        WithdrawalDTO dto = WithdrawalDTO.builder()
                .transactionId(transactionId)
                .reference(reference)
                .amount(amount)
                .fee(fee)
                .netAmount(amount.subtract(fee))
                .currency(currency)
                .currencySymbol(currencySymbol)
                .withdrawalType(type)
                .status(WithdrawalStatus.PENDING)
                .bankCode(bankCode)
                .accountNumber(accountNumber)
                .accountName(accountName)
                .narration(narration)
                .previousBalance(currentBalance)
                .newBalance(currentBalance) 
                .createdAt(LocalDateTime.now())
                .authorizationUrl(authorizationUrl)
                .accessCode(accessCode)
                .paystackReference(paystackReference)
                .build();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success",           true);
        response.put("status",            "PENDING");
        response.put("message",           "Withdrawal initiated. Complete your payment via the authorization URL.");
        response.put("transactionId",     transactionId);
        response.put("reference",         reference);
        response.put("paystackReference", paystackReference);
        response.put("authorizationUrl",  authorizationUrl);
        response.put("accessCode",        accessCode);
        response.put("amount",            amount);
        response.put("fee",               fee);
        response.put("netAmount",         amount.subtract(fee));
        response.put("currency",          currency);
        response.put("currencySymbol",    currencySymbol);
        response.put("currentBalance",    currentBalance);
        response.put("withdrawalType",    type.name());
        response.put("accountHolder",     accountHolder.toUpperCase());
        response.put("accountNumber",     accountNumber);
        response.put("bankCode",          bankCode);
        response.put("accountName",       accountName);
        response.put("email",             email);
        response.put("narration",         narration);
        response.put("timestamp",         java.time.Instant.now().toString());
        response.put("data",              dto);
        return response;
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
        return UUID.randomUUID().toString();
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
