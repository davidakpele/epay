package com.epay.history.service;

import com.epay.domain.history.entity.TransactionHistory;
import com.epay.history.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final TransactionHistoryRepository repository;

    @Transactional
    public void recordDepositInitiated(Long userId, String reference, BigDecimal amount,
                                        String currency, String channel) {
        String key = "INIT_" + reference;
        if (repository.existsByIdempotencyKey(key)) return;

        save(TransactionHistory.builder()
                .transactionId(generateTxnId())
                .userId(userId)
                .reference(reference)
                .transactionType("DEPOSIT")
                .debitCredit("CREDIT")
                .channel(channel)
                .status("PENDING")
                .grossAmount(amount)
                .feeAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .netAmount(amount)
                .currencyType(currency)
                .originalCurrency(currency)
                .exchangeRate(BigDecimal.ONE)
                .category("DEPOSIT")
                .description("DEPOSIT INITIATED via " + channel)
                .idempotencyKey(key)
                .processedAt(now())
                .build());
    }

    @Transactional
    public void recordDepositCompleted(Long userId, Long walletId,
                                        String reference, String gatewayReference,
                                        String transactionId, String channel,
                                        BigDecimal grossAmount, BigDecimal feeAmount,
                                        BigDecimal netAmount, BigDecimal previousBalance,
                                        BigDecimal newBalance, String currency,
                                        String currencySymbol, String accountHolder,
                                        String ipAddress, String deviceId,
                                        String userAgent, String geoLocation,
                                        LocalDateTime completedAt) {
        String key = "COMP_" + reference;
        if (repository.existsByIdempotencyKey(key)) return;

        String uName = upper(accountHolder);
        save(TransactionHistory.builder()
                .transactionId(txnOrGenerate(transactionId))
                .userId(userId)
                .walletId(walletId)
                .accountHolder(uName)
                .reference(reference)
                .gatewayReference(gatewayReference)
                .idempotencyKey(key)
                .transactionType("DEPOSIT")
                .debitCredit("CREDIT")
                .channel(channel)
                .status("SUCCESS")
                .description("DEPOSIT//INTO " + uName + " " + currency + " ACCOUNT")
                .message("Deposited " + grossAmount + " into your " + currency + " wallet.")
                .category("DEPOSIT")
                .grossAmount(grossAmount)
                .feeAmount(safe(feeAmount))
                .taxAmount(BigDecimal.ZERO)
                .netAmount(safe(netAmount))
                .previousBalance(previousBalance)
                .availableBalance(newBalance)
                .runningBalance(newBalance)
                .currencyType(currency)
                .currencySymbol(currencySymbol)
                .originalCurrency(currency)
                .exchangeRate(BigDecimal.ONE)
                .ipAddress(ipAddress)
                .deviceId(deviceId)
                .userAgent(userAgent)
                .geoLocation(geoLocation)
                .riskScore(BigDecimal.ZERO)
                .amlFlag(false)
                .initiatedBy(String.valueOf(userId))
                .adminNote("Deposit of " + currency + " " + grossAmount + " via " + channel)
                .retryCount(0)
                .processedAt(now())
                .completedAt(completedAt != null ? completedAt : LocalDateTime.now())
                .build());
    }

    @Transactional
    public void recordDepositFailed(Long userId, String reference, BigDecimal amount,
                                     String currency, String reason) {
        String key = "FAIL_" + reference;
        if (repository.existsByIdempotencyKey(key)) return;

        save(TransactionHistory.builder()
                .transactionId(generateTxnId())
                .userId(userId)
                .reference(reference)
                .transactionType("DEPOSIT")
                .debitCredit("CREDIT")
                .status("FAILED")
                .grossAmount(safe(amount))
                .feeAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .netAmount(safe(amount))
                .currencyType(currency)
                .originalCurrency(currency)
                .exchangeRate(BigDecimal.ONE)
                .failureReason(reason)
                .description("DEPOSIT FAILED: " + reason)
                .category("DEPOSIT")
                .idempotencyKey(key)
                .processedAt(now())
                .build());
    }

    @Transactional
    public void recordTransferDebit(Long senderUserId, Long senderWalletId,
                                     String transactionId, String reference,
                                     BigDecimal amount, BigDecimal fee,
                                     BigDecimal previousBalance, BigDecimal newBalance,
                                     String currency, String currencySymbol,
                                     String senderName, String recipientName,
                                     Long recipientUserId, Long recipientWalletId,
                                     String ipAddress) {
        String key = "TDEBIT_" + transactionId;
        if (repository.existsByIdempotencyKey(key)) return;

        BigDecimal netAmount = amount.add(safe(fee));
        save(TransactionHistory.builder()
                .transactionId(transactionId)
                .userId(senderUserId)
                .walletId(senderWalletId)
                .accountHolder(upper(senderName))
                .reference(reference)
                .idempotencyKey(key)
                .transactionType("TRANSFER_DEBIT")
                .debitCredit("DEBIT")
                .channel("INTERNAL")
                .status("SUCCESS")
                .description("TRANSFER//FROM " + upper(senderName) + " TO " + upper(recipientName))
                .message("You sent " + amount + " " + currency + " to " + recipientName)
                .category("TRANSFER")
                .grossAmount(amount)
                .feeAmount(safe(fee))
                .taxAmount(BigDecimal.ZERO)
                .netAmount(netAmount)
                .previousBalance(previousBalance)
                .availableBalance(newBalance)
                .runningBalance(newBalance)
                .currencyType(currency)
                .currencySymbol(currencySymbol)
                .originalCurrency(currency)
                .exchangeRate(BigDecimal.ONE)
                .counterpartyUserId(recipientUserId)
                .counterpartyWalletId(recipientWalletId)
                .counterpartyAccountHolder(upper(recipientName))
                .ipAddress(ipAddress)
                .riskScore(BigDecimal.ZERO)
                .amlFlag(false)
                .initiatedBy(String.valueOf(senderUserId))
                .processedAt(now())
                .completedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void recordTransferCredit(Long recipientUserId, Long recipientWalletId,
                                      String transactionId, String reference,
                                      BigDecimal amount, BigDecimal previousBalance,
                                      BigDecimal newBalance, String currency,
                                      String currencySymbol, String senderName,
                                      String recipientName, Long senderUserId,
                                      Long senderWalletId) {
        String key = "TCREDIT_" + transactionId;
        if (repository.existsByIdempotencyKey(key)) return;

        save(TransactionHistory.builder()
                .transactionId(transactionId)
                .userId(recipientUserId)
                .walletId(recipientWalletId)
                .accountHolder(upper(recipientName))
                .reference(reference)
                .idempotencyKey(key)
                .transactionType("TRANSFER_CREDIT")
                .debitCredit("CREDIT")
                .channel("INTERNAL")
                .status("SUCCESS")
                .description("TRANSFER//TO " + upper(recipientName) + " FROM " + upper(senderName))
                .message("You received " + amount + " " + currency + " from " + senderName)
                .category("TRANSFER")
                .grossAmount(amount)
                .feeAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .netAmount(amount)
                .previousBalance(previousBalance)
                .availableBalance(newBalance)
                .runningBalance(newBalance)
                .currencyType(currency)
                .currencySymbol(currencySymbol)
                .originalCurrency(currency)
                .exchangeRate(BigDecimal.ONE)
                .counterpartyUserId(senderUserId)
                .counterpartyWalletId(senderWalletId)
                .counterpartyAccountHolder(upper(senderName))
                .riskScore(BigDecimal.ZERO)
                .amlFlag(false)
                .processedAt(now())
                .completedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void recordWithdrawal(Long userId, Long walletId,
                                  String transactionId, String reference,
                                  String gatewayReference, String channel,
                                  String status, BigDecimal amount, BigDecimal fee,
                                  BigDecimal previousBalance, BigDecimal newBalance,
                                  String currency, String currencySymbol,
                                  String accountHolder, String bankCode,
                                  String bankAccountNumber, String ipAddress,
                                  String failureReason) {
        String key = "WITH_" + transactionId;
        if (repository.existsByIdempotencyKey(key)) return;

        save(TransactionHistory.builder()
                .transactionId(txnOrGenerate(transactionId))
                .userId(userId)
                .walletId(walletId)
                .accountHolder(upper(accountHolder))
                .reference(reference)
                .gatewayReference(gatewayReference)
                .idempotencyKey(key)
                .transactionType("WITHDRAWAL")
                .debitCredit("DEBIT")
                .channel(channel)
                .status(status)
                .description("WITHDRAWAL//FROM " + upper(accountHolder) + " " + currency + " ACCOUNT")
                .message("Withdrawal of " + amount + " " + currency + " via " + channel)
                .category("WITHDRAWAL")
                .grossAmount(amount)
                .feeAmount(safe(fee))
                .taxAmount(BigDecimal.ZERO)
                .netAmount(amount.subtract(safe(fee)))
                .previousBalance(previousBalance)
                .availableBalance(newBalance)
                .runningBalance(newBalance)
                .currencyType(currency)
                .currencySymbol(currencySymbol)
                .originalCurrency(currency)
                .exchangeRate(BigDecimal.ONE)
                .bankCode(bankCode)
                .bankAccountNumber(bankAccountNumber)
                .ipAddress(ipAddress)
                .failureReason(failureReason)
                .riskScore(BigDecimal.ZERO)
                .amlFlag(false)
                .initiatedBy(String.valueOf(userId))
                .processedAt(now())
                .completedAt("SUCCESS".equals(status) ? LocalDateTime.now() : null)
                .build());
    }

    @Transactional
    public void recordSwap(Long userId, Long walletId,
                            String transactionId, String reference,
                            BigDecimal fromAmount, BigDecimal toAmount,
                            String fromCurrency, String toCurrency,
                            BigDecimal exchangeRate, BigDecimal fee,
                            BigDecimal previousBalance, BigDecimal newBalance,
                            String accountHolder) {
        String key = "SWAP_" + transactionId;
        if (repository.existsByIdempotencyKey(key)) return;

        save(TransactionHistory.builder()
                .transactionId(txnOrGenerate(transactionId))
                .userId(userId)
                .walletId(walletId)
                .accountHolder(upper(accountHolder))
                .reference(reference)
                .idempotencyKey(key)
                .transactionType("SWAP")
                .debitCredit("DEBIT")
                .channel("INTERNAL")
                .status("SUCCESS")
                .description("SWAP " + fromCurrency + " TO " + toCurrency)
                .message("Swapped " + fromAmount + " " + fromCurrency + " to " + toAmount + " " + toCurrency)
                .category("SWAP")
                .grossAmount(fromAmount)
                .feeAmount(safe(fee))
                .taxAmount(BigDecimal.ZERO)
                .netAmount(toAmount)
                .previousBalance(previousBalance)
                .availableBalance(newBalance)
                .runningBalance(newBalance)
                .currencyType(fromCurrency)
                .currencySymbol(fromCurrency)
                .originalCurrency(fromCurrency)
                .exchangeRate(safe(exchangeRate))
                .riskScore(BigDecimal.ZERO)
                .amlFlag(false)
                .initiatedBy(String.valueOf(userId))
                .processedAt(now())
                .completedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void recordMaintenanceFee(Long userId, Long walletId,
                                      String transactionId, String referenceNo,
                                      BigDecimal amount, BigDecimal previousBalance,
                                      BigDecimal newBalance, String currency,
                                      String currencySymbol, String accountHolder,
                                      String reason) {
        String key = "MAINT_" + transactionId;
        if (repository.existsByIdempotencyKey(key)) return;

        save(TransactionHistory.builder()
                .transactionId(txnOrGenerate(transactionId))
                .userId(userId)
                .walletId(walletId)
                .accountHolder(upper(accountHolder))
                .reference(referenceNo)
                .idempotencyKey(key)
                .transactionType("FEE")
                .debitCredit("DEBIT")
                .channel("SYSTEM")
                .status("SUCCESS")
                .description("MAINTENANCE FEE DEDUCTION")
                .message("Maintenance fee of " + amount + " " + currency + " deducted.")
                .category("FEE")
                .grossAmount(amount)
                .feeAmount(amount)
                .taxAmount(BigDecimal.ZERO)
                .netAmount(amount)
                .previousBalance(previousBalance)
                .availableBalance(newBalance)
                .runningBalance(newBalance)
                .currencyType(currency)
                .currencySymbol(currencySymbol)
                .originalCurrency(currency)
                .exchangeRate(BigDecimal.ONE)
                .adminNote(reason)
                .initiatedBy("SYSTEM")
                .manualAdjustmentFlag(false)
                .riskScore(BigDecimal.ZERO)
                .amlFlag(false)
                .processedAt(now())
                .completedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void recordInternalOperation(Long userId, Long walletId,
                                         String transactionId, String referenceNo,
                                         String transactionType, String debitCredit,
                                         BigDecimal amount, BigDecimal previousBalance,
                                         BigDecimal newBalance, String currency,
                                         String currencySymbol, String description) {
        String key = transactionType + "_" + transactionId;
        if (repository.existsByIdempotencyKey(key)) return;

        save(TransactionHistory.builder()
                .transactionId(txnOrGenerate(transactionId))
                .userId(userId)
                .walletId(walletId)
                .reference(referenceNo)
                .idempotencyKey(key)
                .transactionType(transactionType)
                .debitCredit(debitCredit)
                .channel("SYSTEM")
                .status("SUCCESS")
                .description(description)
                .category(transactionType)
                .grossAmount(amount)
                .feeAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .netAmount(amount)
                .previousBalance(previousBalance)
                .availableBalance(newBalance)
                .runningBalance(newBalance)
                .currencyType(currency)
                .currencySymbol(currencySymbol)
                .originalCurrency(currency)
                .exchangeRate(BigDecimal.ONE)
                .riskScore(BigDecimal.ZERO)
                .amlFlag(false)
                .initiatedBy("SYSTEM")
                .processedAt(now())
                .completedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void record(Long userId, Long walletId,
                        String transactionId, String reference,
                        String transactionType, String debitCredit,
                        String channel, String status,
                        BigDecimal grossAmount, BigDecimal feeAmount,
                        BigDecimal netAmount, BigDecimal previousBalance,
                        BigDecimal newBalance, String currency, String currencySymbol,
                        String accountHolder, String description,
                        String counterpartyAccountHolder, Long counterpartyUserId,
                        Long counterpartyWalletId, String ipAddress,
                        String deviceId, String userAgent,
                        String adminNote, LocalDateTime completedAt) {

        String key = transactionType + "_" + txnOrGenerate(transactionId);
        if (repository.existsByIdempotencyKey(key)) return;

        save(TransactionHistory.builder()
                .transactionId(txnOrGenerate(transactionId))
                .userId(userId)
                .walletId(walletId)
                .accountHolder(upper(accountHolder))
                .reference(reference)
                .idempotencyKey(key)
                .transactionType(transactionType)
                .debitCredit(debitCredit)
                .channel(channel)
                .status(status)
                .description(description)
                .category(transactionType)
                .grossAmount(safe(grossAmount))
                .feeAmount(safe(feeAmount))
                .taxAmount(BigDecimal.ZERO)
                .netAmount(safe(netAmount))
                .previousBalance(previousBalance)
                .availableBalance(newBalance)
                .runningBalance(newBalance)
                .currencyType(currency)
                .currencySymbol(currencySymbol)
                .originalCurrency(currency)
                .exchangeRate(BigDecimal.ONE)
                .counterpartyAccountHolder(counterpartyAccountHolder)
                .counterpartyUserId(counterpartyUserId)
                .counterpartyWalletId(counterpartyWalletId)
                .ipAddress(ipAddress)
                .deviceId(deviceId)
                .userAgent(userAgent)
                .adminNote(adminNote)
                .riskScore(BigDecimal.ZERO)
                .amlFlag(false)
                .processedAt(now())
                .completedAt(completedAt != null ? completedAt : LocalDateTime.now())
                .build());
    }

    public Page<TransactionHistory> getByUserId(Long userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable);
    }

    public Page<TransactionHistory> getByUserIdAndType(Long userId, String type, Pageable pageable) {
        return repository.findByUserIdAndType(userId, type.toUpperCase(), pageable);
    }

    public Page<TransactionHistory> getByWalletId(Long walletId, Pageable pageable) {
        return repository.findByWalletId(walletId, pageable);
    }

    public Optional<TransactionHistory> getByReference(String reference) {
        return repository.findByReference(reference);
    }

    public Optional<TransactionHistory> getByTransactionId(String transactionId) {
        return repository.findByTransactionId(transactionId);
    }

    public List<TransactionHistory> getByUserIdAndDateRange(Long userId,
                                                              LocalDateTime from,
                                                              LocalDateTime to) {
        return repository.findByUserIdAndDateRange(userId, from, to);
    }

    public java.math.BigDecimal sumByUserIdAndType(Long userId, String type) {
        java.math.BigDecimal result = repository.sumByUserIdAndType(userId, type.toUpperCase());
        return result != null ? result : BigDecimal.ZERO;
    }

    private void save(TransactionHistory record) {
        try {
            repository.save(record);
        } catch (Exception e) {
            log.error("[History] Failed to save record type={} userId={}: {}",
                    record.getTransactionType(), record.getUserId(), e.getMessage());
        }
    }

    private String generateTxnId() {
        long hash = Math.abs(UUID.randomUUID().getMostSignificantBits());
        return "TXN_" + String.valueOf(hash).substring(0, 10);
    }

    private String txnOrGenerate(String txnId) {
        return (txnId != null && !txnId.isBlank()) ? txnId : generateTxnId();
    }

    private String now() {
        return Instant.now().toString();
    }

    private String upper(String value) {
        return value != null ? value.toUpperCase() : "";
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
