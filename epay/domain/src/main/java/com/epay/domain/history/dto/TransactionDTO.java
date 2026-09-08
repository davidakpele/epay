package com.epay.domain.history.dto;

import com.epay.domain.history.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * API response DTO for a single transaction.
 *
 * <p>Shape mirrors the Enterprise History Design contract:
 * <pre>
 * {
 *   "transactionId":      "NX872403150",
 *   "reference":          "DEP_CARD_2_…",
 *   "idempotencyKey":     "…",
 *   "correlationId":      "…",
 *   "transactionType":    "DEPOSIT",
 *   "transactionCategory":"WALLET_FUNDING",
 *   "debitCredit":        "CREDIT",
 *   "channel":            "CARD",
 *   "status":             "DELIVERED",
 *   "stateMachine":       { "currentState": "…", "states": {…}, "allowedTransitions": […] },
 *   "statusHistory":      [ {…}, … ],
 *   "amount":             { "currency":"NGN", "symbol":"₦", "gross":…, "fee":…, "tax":…, "net":… },
 *   "balance":            { "currency":"NGN", "previous":…, "change":…, "running":…, "available":… },
 *   "provider":           { "name":"PAYSTACK", … },
 *   "settlement":         { "status":"SETTLED", … },
 *   "failure":            null,
 *   "description":        "…",
 *   "metadata":           { "walletId":1, "accountType":"NGN", "paymentMethod":"CARD" },
 *   "relatedTransactions":[],
 *   "ledger":             { "ledgerEntryId":"…", "ledgerStatus":"POSTED" },
 *   "user":               { "id":2, "accountHolder":"…", "walletId":1 },
 *   "timestamps":         { "createdAt":"…", "updatedAt":"…", "completedAt":"…" }
 * }
 * </pre>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {

    // ── Identity ─────────────────────────────────────────────────────────────
    /** DB primary key of the transactions row — exposed so callers can reference this record directly. */
    private Long   historyId;
    private String transactionId;
    private String reference;
    private String idempotencyKey;
    private String correlationId;

    /**
     * Visibility flag — mirrors {@code transactions.publish_access}.
     * {@code true}  = record is visible to the user (default).
     * {@code false} = user soft-deleted this record; hidden from their history
     *                 but still accessible to admin/compliance.
     * Only populated in admin responses; omitted (null) in standard user responses.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean publishAccess;

    // ── Classification ───────────────────────────────────────────────────────
    private String transactionType;
    private String transactionCategory;
    private String debitCredit;
    private String channel;

    // ── Status ───────────────────────────────────────────────────────────────
    /** Current / final status string (mirrors stateMachine.currentState). */
    private TransactionStatus status;

    /** Full state-machine graph including unvisited states and allowed transitions. */
    private StateMachine stateMachine;

    /** Ordered list of states that were actually visited — for timeline rendering. */
    private List<StatusHistoryEntry> statusHistory;

    // ── Money ────────────────────────────────────────────────────────────────
    private AmountInfo  amount;
    private BalanceInfo balance;

    // ── Provider ─────────────────────────────────────────────────────────────
    private ProviderInfo provider;

    // ── Settlement ───────────────────────────────────────────────────────────
    private SettlementInfo settlement;

    // ── Failure ──────────────────────────────────────────────────────────────
    /** Populated only when status == FAILED; null otherwise. */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private FailureInfo failure;

    // ── Description / free-text ──────────────────────────────────────────────
    private String description;

    // ── Extra structured fields ──────────────────────────────────────────────
    private MetadataInfo        metadata;
    private List<String>        relatedTransactions;
    private LedgerInfo          ledger;

    // ── Parties ──────────────────────────────────────────────────────────────
    private UserInfo user;
    private UserInfo recipient;

    // ── Timestamps ───────────────────────────────────────────────────────────
    private TimestampsInfo timestamps;

    // =========================================================================
    // Nested DTOs
    // =========================================================================

    @Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private Long   id;
        private Long   walletId;
        private String accountHolder;
    }

    @Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AmountInfo {
        private String     currency;
        private String     symbol;
        private BigDecimal gross;
        private BigDecimal fee;
        private BigDecimal tax;
        private BigDecimal net;
    }

    @Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BalanceInfo {
        private String     currency;
        private BigDecimal previous;
        private BigDecimal change;
        private BigDecimal running;
        private BigDecimal available;
    }

    @Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProviderInfo {
        private String name;
        private String providerReference;
        private String providerTransactionId;
        private String providerStatus;
    }

    @Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SettlementInfo {
        private String  status;
        private String  settlementReference;
        private Instant settledAt;
    }

    @Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FailureInfo {
        private String code;
        private String reason;
        private Instant failedAt;
    }

    @Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetadataInfo {
        private Long   walletId;
        private String accountType;
        private String paymentMethod;
    }

    @Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LedgerInfo {
        private String ledgerEntryId;
        private String ledgerStatus;
    }

    @Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TimestampsInfo {
        private Instant createdAt;
        private Instant updatedAt;
        private Instant completedAt;
    }
}
