package com.epay.history.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epay.domain.history.dto.StatusTimeline;
import com.epay.domain.history.dto.TransactionDTO;
import com.epay.domain.history.entity.Transaction;
import com.epay.domain.history.entity.TransactionAuditLog;
import com.epay.domain.history.enums.TransactionStatus;
import com.epay.history.repository.AuditLogRepository;
import com.epay.history.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final TransactionRepository  transactionRepository;
    private final AuditLogRepository     auditLogRepository;

    // -----------------------------------------------------------------------
    // Core write — called by adapters
    // -----------------------------------------------------------------------

    /**
     * Creates a new transaction record and builds a structured status timeline in one call.
     *
     * <p>The timeline follows this flow for every transaction type:
     * <pre>
     *   INITIATED  — request received, basic validation passed
     *   PROCESSED  — funds verified, rules passed, ready to execute
     *   PENDING    — (non-INTERNAL only) submitted to gateway / awaiting settlement
     *   DELIVERED  — funds confirmed and credited/debited successfully
     *      or
     *   SETTLED    — ledger balance updated (internal bookkeeping variant)
     *      or
     *   FAILED     — stops at the step where the failure occurred:
     *                  "FAILED"              → failed at INITIATED (pre-validation)
     *                  "FAILED_AT_PROCESSED" → failed after PROCESSED (gateway reject)
     *                  "FAILED_AT_PENDING"   → failed after PENDING  (settlement failure)
     * </pre>
     *
     * <p>INTERNAL channel transactions (wallet-to-wallet, swap) skip PENDING because
     * there is no external gateway — funds move atomically inside the ledger.
     */
    @Transactional
    public Transaction record(RecordRequest req) {
        // Dedup by transactionId — Redis already blocked duplicate submissions upstream.
        // This is a safety net only for retries that slip through.
        if (req.transactionId() != null
                && transactionRepository.existsByTransactionId(req.transactionId())) {
            log.warn("[History] Duplicate transactionId={} — skipping insert", req.transactionId());
            return transactionRepository.findByTransactionId(req.transactionId()).orElse(null);
        }

        String txnId   = txnOrGenerate(req.transactionId());
        String actor   = resolveActor(req.channel());
        boolean isInternal = "INTERNAL".equalsIgnoreCase(req.channel());

        StatusTimeline timeline  = new StatusTimeline();
        TransactionStatus finalStatus = parseStatus(req.status());

        // ── Step 1: INITIATED — always present ──────────────────────────────
        timeline.add(TransactionStatus.INITIATED, "SYSTEM", "Transaction created");

        if (finalStatus == TransactionStatus.FAILED && isEarlyFailure(req.status())) {
            // Failed before any processing — stop here
        } else {
            // ── Step 2: PROCESSED — validation passed, ready to execute ─────
            timeline.add(TransactionStatus.PROCESSED, "SYSTEM",
                    processedMessage(req.transactionType()));

            if (finalStatus == TransactionStatus.FAILED
                    && "FAILED_AT_PROCESSED".equalsIgnoreCase(req.status())) {
                // Failed at gateway submission — stop after PROCESSED
            } else {
                // ── Step 3: PENDING — awaiting external gateway (skip for INTERNAL) ─
                if (!isInternal) {
                    timeline.add(TransactionStatus.PENDING, "SYSTEM",
                            "Submitted to " + actor + ", awaiting confirmation");
                }

                // ── Step 4: Final status (DELIVERED / SETTLED / FAILED / CANCELLED …) ─
                if (finalStatus != TransactionStatus.INITIATED
                        && finalStatus != TransactionStatus.PROCESSED
                        && finalStatus != TransactionStatus.PENDING) {
                    timeline.add(finalStatus, actor,
                            statusMessage(finalStatus, req.transactionType()));
                }
            }
        }

        Transaction txn = Transaction.builder()
                .transactionId(txnId)
                .reference(req.reference())
                .idempotencyKey(req.idempotencyKey())
                .userId(req.userId())
                .walletId(req.walletId())
                .accountHolder(upper(req.accountHolder()))
                .counterpartyUserId(req.counterpartyUserId())
                .counterpartyWalletId(req.counterpartyWalletId())
                .counterpartyAccountHolder(upper(req.counterpartyAccountHolder()))
                .transactionType(req.transactionType())
                .debitCredit(req.debitCredit())
                .channel(req.channel())
                .currentStatus(finalStatus)
                .statusTimeline(timeline)
                .grossAmount(safe(req.grossAmount()))
                .feeAmount(safe(req.feeAmount()))
                .taxAmount(BigDecimal.ZERO)
                .netAmount(safe(req.netAmount()))
                .previousBalance(req.previousBalance())
                .availableBalance(req.newBalance())
                .runningBalance(req.newBalance())
                .currency(req.currency())
                .currencySymbol(req.currencySymbol())
                .exchangeRate(BigDecimal.ONE)
                .description(req.description())
                .failureReason(req.failureReason())
                .ipAddress(req.ipAddress())
                .deviceId(req.deviceId())
                .userAgent(req.userAgent())
                .adminNote(req.adminNote())
                .completedAt(isTerminal(finalStatus) ? LocalDateTime.now() : req.completedAt())
                .build();

        transactionRepository.save(txn);

        // Append audit record
        audit(txnId, "CREATE", "SYSTEM", null, finalStatus,
                req.ipAddress(), req.deviceId(), null);

        log.info("[History] Recorded txn={} type={} status={} userId={}",
                txnId, req.transactionType(), finalStatus, req.userId());
        return txn;
    }

    /**
     * Advances an existing transaction to a new status.
     * Updates statusTimeline + currentStatus, appends an audit log entry.
     */
    @Transactional
    public Transaction advanceStatus(String transactionId, TransactionStatus newStatus,
                                      String actor, String message,
                                      String ipAddress, String deviceId, String reason) {
        Transaction txn = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found: " + transactionId));

        TransactionStatus previous = txn.getCurrentStatus();
        txn.advanceStatus(newStatus, actor, message);
        transactionRepository.save(txn);

        audit(transactionId, "STATUS_CHANGE", actor, previous, newStatus,
                ipAddress, deviceId, reason);

        log.info("[History] Status {} → {} for txn={}", previous, newStatus, transactionId);
        return txn;
    }

    // -----------------------------------------------------------------------
    // Read methods
    // -----------------------------------------------------------------------

    public Page<TransactionDTO> getByUserId(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable).map(this::toDTO);
    }

    public Page<TransactionDTO> getByUserIdAndType(Long userId, String type, Pageable pageable) {
        return transactionRepository.findByUserIdAndType(userId, type.toUpperCase(), pageable)
                .map(this::toDTO);
    }

    public Page<TransactionDTO> getByUserIdAndStatus(Long userId, TransactionStatus status,
                                                       Pageable pageable) {
        return transactionRepository.findByUserIdAndStatus(userId, status, pageable)
                .map(this::toDTO);
    }

    public Page<TransactionDTO> getByWalletId(Long walletId, Pageable pageable) {
        return transactionRepository.findByWalletId(walletId, pageable).map(this::toDTO);
    }

    public Optional<TransactionDTO> getByTransactionId(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId).map(this::toDTO);
    }

    public Optional<TransactionDTO> getByReference(String reference) {
        return transactionRepository.findByReference(reference).map(this::toDTO);
    }

    public List<TransactionDTO> getByDateRange(Long userId, LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findByUserIdAndDateRange(userId, from, to)
                .stream().map(this::toDTO).toList();
    }

    public List<TransactionAuditLog> getAuditLog(String transactionId) {
        return auditLogRepository.findByTransactionId(transactionId);
    }

    public BigDecimal sumDeliveredByType(Long userId, String type) {
        BigDecimal result = transactionRepository.sumDeliveredByUserIdAndType(userId, type.toUpperCase());
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * Flexible filter for GET /history/user/{userId}/filter
     *
     * @param transactionType "ALL" to skip type filter, or a specific type e.g. "DEPOSIT", "TRANSFER_DEBIT"
     * @param currency        null or a currency code e.g. "NGN"
     * @param status          null to return all statuses, or a specific TransactionStatus
     * @param fromDate        inclusive start of date range
     * @param toDate          inclusive end of date range (set to end-of-day internally)
     */
    public Page<TransactionDTO> filterByUser(Long userId,
                                              LocalDateTime fromDate,
                                              LocalDateTime toDate,
                                              String transactionType,
                                              String currency,
                                              TransactionStatus status,
                                              Pageable pageable) {
        String type     = (transactionType == null || transactionType.isBlank()) ? "ALL"
                          : transactionType.toUpperCase();
        String cur      = (currency == null || currency.isBlank()) ? null
                          : currency.toUpperCase();
        // Ensure toDate covers the full end-of-day if only a date was supplied
        LocalDateTime endOfDay = toDate.getHour() == 0 && toDate.getMinute() == 0
                ? toDate.withHour(23).withMinute(59).withSecond(59)
                : toDate;

        return transactionRepository
                .filterByUser(userId, fromDate, endOfDay, type, cur, status, pageable)
                .map(this::toDTO);
    }

    // -----------------------------------------------------------------------
    // Mapper
    // -----------------------------------------------------------------------

    private TransactionDTO toDTO(Transaction t) {
        return TransactionDTO.builder()
                .transactionId(t.getTransactionId())
                .reference(t.getReference())
                .transactionType(t.getTransactionType())
                .debitCredit(t.getDebitCredit())
                .channel(t.getChannel())
                .user(TransactionDTO.UserInfo.builder()
                        .id(t.getUserId())
                        .walletId(t.getWalletId())
                        .accountHolder(t.getAccountHolder())
                        .build())
                .recipient(t.getCounterpartyUserId() != null
                        ? TransactionDTO.UserInfo.builder()
                                .id(t.getCounterpartyUserId())
                                .walletId(t.getCounterpartyWalletId())
                                .accountHolder(t.getCounterpartyAccountHolder())
                                .build()
                        : null)
                .amount(TransactionDTO.AmountInfo.builder()
                        .gross(t.getGrossAmount())
                        .fee(t.getFeeAmount())
                        .tax(t.getTaxAmount())
                        .net(t.getNetAmount())
                        .currency(t.getCurrency())
                        .symbol(t.getCurrencySymbol())
                        .build())
                .balance(TransactionDTO.BalanceInfo.builder()
                        .previous(t.getPreviousBalance())
                        .available(t.getAvailableBalance())
                        .running(t.getRunningBalance())
                        .build())
                .currentStatus(t.getCurrentStatus())
                .statusTimeline(t.getStatusTimeline())
                .description(t.getDescription())
                .failureReason(t.getFailureReason())
                .createdAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build();
    }

    // -----------------------------------------------------------------------
    // Audit helper
    // -----------------------------------------------------------------------

    private void audit(String txnId, String action, String performedBy,
                        TransactionStatus previous, TransactionStatus newStatus,
                        String ip, String deviceId, String reason) {
        try {
            auditLogRepository.save(TransactionAuditLog.builder()
                    .transactionId(txnId)
                    .action(action)
                    .performedBy(performedBy != null ? performedBy : "SYSTEM")
                    .previousStatus(previous)
                    .newStatus(newStatus)
                    .ipAddress(ip)
                    .deviceId(deviceId)
                    .reason(reason)
                    .build());
        } catch (Exception e) {
            log.warn("[Audit] Failed to write audit for txn={}: {}", txnId, e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String txnOrGenerate(String id) {
        if (id != null && !id.isBlank()) return id;
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "TXN-" + date + "-" + rand;
    }

    /**
     * Maps a raw status string (as passed by callers) to the canonical {@link TransactionStatus}.
     *
     * <p>Special failure variants:
     * <ul>
     *   <li>{@code "FAILED"}              — failed very early (pre-processing)</li>
     *   <li>{@code "FAILED_AT_PROCESSED"} — failed after processing, before gateway dispatch</li>
     *   <li>{@code "FAILED_AT_PENDING"}   — failed after gateway submission (settlement failure)</li>
     * </ul>
     * All three map to {@link TransactionStatus#FAILED}; the timeline builder uses the raw string
     * to decide where to stop the chain.
     */
    private TransactionStatus parseStatus(String status) {
        if (status == null) return TransactionStatus.DELIVERED;
        return switch (status.toUpperCase()) {
            case "SUCCESS", "COMPLETED", "DELIVERED"  -> TransactionStatus.DELIVERED;
            case "SETTLED"                            -> TransactionStatus.SETTLED;
            case "PENDING"                            -> TransactionStatus.PENDING;
            case "PROCESSED"                          -> TransactionStatus.PROCESSED;
            case "PROCESSING"                         -> TransactionStatus.PROCESSING;
            case "FAILED",
                 "FAILED_AT_PROCESSED",
                 "FAILED_AT_PENDING"                  -> TransactionStatus.FAILED;
            case "CANCELLED"                          -> TransactionStatus.CANCELLED;
            case "REVERSED"                           -> TransactionStatus.REVERSED;
            default                                   -> TransactionStatus.DELIVERED;
        };
    }

    /**
     * Returns {@code true} when the raw status indicates the transaction failed
     * before any processing step was reached (i.e. timeline should stop at INITIATED).
     */
    private boolean isEarlyFailure(String rawStatus) {
        if (rawStatus == null) return false;
        String s = rawStatus.toUpperCase();
        // Only plain "FAILED" with no qualifier = early/pre-processing failure
        return s.equals("FAILED");
    }

    private String resolveActor(String channel) {
        if (channel == null) return "SYSTEM";
        return switch (channel.toUpperCase()) {
            case "PAYSTACK"    -> "PAYSTACK";
            case "FLUTTERWAVE" -> "FLUTTERWAVE";
            case "CARD"        -> "PAYSTACK";   // card payments route through Paystack
            case "USSD"        -> "PAYSTACK";   // USSD routes through Paystack
            case "BANK",
                 "BANK_TRANSFER" -> "BANK";
            case "INTERNAL"    -> "LEDGER";
            default            -> "SYSTEM";
        };
    }

    /** Message shown at the PROCESSED step — describes what was validated. */
    private String processedMessage(String type) {
        if (type == null) return "Request validated and processed";
        return switch (type.toUpperCase()) {
            case "DEPOSIT"                    -> "Deposit request validated, funds pending credit";
            case "WITHDRAWAL", "WITHDRAWAL_DEBIT",
                 "WITHDRAWAL_CREDIT"          -> "Withdrawal request validated, funds reserved";
            case "TRANSFER_DEBIT"             -> "Transfer validated, funds debited from sender";
            case "TRANSFER_CREDIT"            -> "Transfer validated, funds credited to recipient";
            case "SWAP"                       -> "Swap validated, exchange rate locked";
            default                           -> "Request validated and processed";
        };
    }

    private String statusMessage(TransactionStatus status, String type) {
        String t = type != null ? type : "Transaction";
        return switch (status) {
            case DELIVERED  -> t + " completed successfully";
            case SETTLED    -> "Ledger updated — " + t.toLowerCase() + " settled";
            case FAILED     -> t + " failed";
            case CANCELLED  -> t + " cancelled";
            case REVERSED   -> t + " reversed";
            case PROCESSING -> "Processing with gateway";
            case PROCESSED  -> processedMessage(type);
            case PENDING    -> "Awaiting gateway confirmation";
            default         -> status.name();
        };
    }

    private boolean isTerminal(TransactionStatus status) {
        return status == TransactionStatus.DELIVERED ||
               status == TransactionStatus.SETTLED   ||
               status == TransactionStatus.FAILED    ||
               status == TransactionStatus.CANCELLED ||
               status == TransactionStatus.REVERSED  ||
               status == TransactionStatus.EXPIRED;
    }

    private String upper(String v) { return v != null ? v.toUpperCase() : null; }
    private BigDecimal safe(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }

    // -----------------------------------------------------------------------
    // Request record — replaces the sprawling method signature
    // -----------------------------------------------------------------------

    public record RecordRequest(
            Long userId,
            Long walletId,
            String transactionId,
            String reference,
            String idempotencyKey,
            String transactionType,
            String debitCredit,
            String channel,
            String status,
            BigDecimal grossAmount,
            BigDecimal feeAmount,
            BigDecimal netAmount,
            BigDecimal previousBalance,
            BigDecimal newBalance,
            String currency,
            String currencySymbol,
            String accountHolder,
            String description,
            String failureReason,
            String counterpartyAccountHolder,
            Long counterpartyUserId,
            Long counterpartyWalletId,
            String ipAddress,
            String deviceId,
            String userAgent,
            String adminNote,
            LocalDateTime completedAt
    ) {
        /** Convenience builder-style factory — no idempotencyKey parameter.
         *  Idempotency is enforced upstream via Redis (RedisIdempotencyService).
         *  The transactionId itself acts as the natural dedup key inside history.
         */
        public static RecordRequest of(Long userId, Long walletId,
                                        String transactionId, String reference,
                                        String transactionType, String debitCredit,
                                        String channel, String status,
                                        BigDecimal grossAmount, BigDecimal feeAmount,
                                        BigDecimal netAmount, BigDecimal previousBalance,
                                        BigDecimal newBalance, String currency,
                                        String currencySymbol, String accountHolder,
                                        String description,
                                        String counterpartyAccountHolder,
                                        Long counterpartyUserId, Long counterpartyWalletId,
                                        String ipAddress, String deviceId, String userAgent,
                                        String adminNote, LocalDateTime completedAt) {
            return new RecordRequest(userId, walletId, transactionId, reference,
                    null,   // idempotencyKey — not used; Redis handles dedup externally
                    transactionType, debitCredit, channel, status,
                    grossAmount, feeAmount, netAmount, previousBalance, newBalance,
                    currency, currencySymbol, accountHolder, description, null,
                    counterpartyAccountHolder, counterpartyUserId, counterpartyWalletId,
                    ipAddress, deviceId, userAgent, adminNote, completedAt);
        }
    }
}
