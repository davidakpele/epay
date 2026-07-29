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
     * Creates a new transaction record with INITIATED → final status in one call.
     * The timeline is built automatically:
     *   INITIATED (SYSTEM) → optional PENDING → final status
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

        String txnId = txnOrGenerate(req.transactionId());

        StatusTimeline timeline = new StatusTimeline();
        timeline.add(TransactionStatus.INITIATED, "SYSTEM", "Transaction created");

        // For async/gateway flows add PENDING
        if (req.channel() != null && !req.channel().equals("INTERNAL")) {
            timeline.add(TransactionStatus.PENDING, "SYSTEM", "Awaiting gateway");
        }

        // Final status
        TransactionStatus finalStatus = parseStatus(req.status());
        if (finalStatus != TransactionStatus.INITIATED && finalStatus != TransactionStatus.PENDING) {
            String actor = resolveActor(req.channel());
            timeline.add(finalStatus, actor, statusMessage(finalStatus, req.transactionType()));
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

    private TransactionStatus parseStatus(String status) {
        if (status == null) return TransactionStatus.DELIVERED;
        return switch (status.toUpperCase()) {
            case "SUCCESS", "COMPLETED", "DELIVERED" -> TransactionStatus.DELIVERED;
            case "PENDING"                           -> TransactionStatus.PENDING;
            case "PROCESSING"                        -> TransactionStatus.PROCESSING;
            case "FAILED"                            -> TransactionStatus.FAILED;
            case "CANCELLED"                         -> TransactionStatus.CANCELLED;
            case "REVERSED"                          -> TransactionStatus.REVERSED;
            case "SETTLED"                           -> TransactionStatus.SETTLED;
            default                                  -> TransactionStatus.DELIVERED;
        };
    }

    private String resolveActor(String channel) {
        if (channel == null) return "SYSTEM";
        return switch (channel.toUpperCase()) {
            case "PAYSTACK"   -> "PAYSTACK";
            case "FLUTTERWAVE"-> "FLUTTERWAVE";
            case "BANK"       -> "BANK";
            case "INTERNAL"   -> "LEDGER";
            default           -> "SYSTEM";
        };
    }

    private String statusMessage(TransactionStatus status, String type) {
        return switch (status) {
            case DELIVERED  -> type + " completed successfully";
            case FAILED     -> type + " failed";
            case CANCELLED  -> type + " cancelled";
            case REVERSED   -> type + " reversed";
            case SETTLED    -> "Ledger updated";
            case PROCESSING -> "Gateway processing";
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
