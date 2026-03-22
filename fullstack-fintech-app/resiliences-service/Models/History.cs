using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using resiliences_service.Enums;

namespace resiliences_service.Models
{
    [Table("Histories")]
    public class History
    {
        // ── Core Identity ──────────────────────────────────────────────
        [Key]
        [Column(TypeName = "char(36)")]
        [MaxLength(36)]
        public string Id { get; set; } = Guid.NewGuid().ToString();

        [Required]
        public ulong WalletId { get; set; }

        [Required]
        public ulong UserId { get; set; }

        public string? SessionId { get; set; }

        public string? TransactionId { get; set; }

        [Column("referenceNo")]
        public string? ReferenceId { get; set; }

        public string? TerminalId { get; set; }

        public string? ErId { get; set; }

        public string? AccountHolder { get; set; }

        [Column(TypeName = "varchar(50)")]
        public TransactionType? Type { get; set; }

        public string? Description { get; set; }

        public string? Message { get; set; }

        [Required]
        [Column(TypeName = "varchar(10)")]
        public string CurrencyType { get; set; } = default!;

        [Column(TypeName = "varchar(20)")]
        public string? Status { get; set; }

        [Column("ip_address")]
        [MaxLength(45)]
        public string? IpAddress { get; set; }

        public DateTime? Timestamp { get; set; }

        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public DateTime CreatedOn { get; set; } = DateTime.UtcNow;

        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public DateTime UpdatedOn { get; set; } = DateTime.UtcNow;

        // ── Financial Amounts (decimal — never double for money) ────────
        [Column(TypeName = "decimal(18,4)")]
        public decimal GrossAmount { get; set; }

        [Column(TypeName = "decimal(18,4)")]
        public decimal FeeAmount { get; set; }

        [Column(TypeName = "decimal(18,4)")]
        public decimal TaxAmount { get; set; }

        [Column(TypeName = "decimal(18,4)")]
        public decimal NetAmount { get; set; }

        [Column(TypeName = "decimal(18,4)")]
        public decimal PreviousBalance { get; set; }

        [Column(TypeName = "decimal(18,4)")]
        public decimal AvailableBalance { get; set; }

        [Column(TypeName = "decimal(18,4)")]
        public decimal RunningBalance { get; set; }

        // ── Double-Entry Accounting ─────────────────────────────────────
        [Column(TypeName = "varchar(10)")]
        public DebitCredit? DebitCredit { get; set; }

        [Column(TypeName = "varchar(30)")]
        public LedgerEntryType? LedgerEntryType { get; set; }

        // ── Counterparty & Routing ──────────────────────────────────────
        public ulong? CounterpartyWalletId { get; set; }

        public ulong? CounterpartyUserId { get; set; }

        [MaxLength(20)]
        public string? CounterpartyAccountHolder { get; set; }

        [MaxLength(20)]
        public string? BankCode { get; set; }

        [MaxLength(30)]
        public string? BankAccountNumber { get; set; }

        [MaxLength(20)]
        public string? RoutingNumber { get; set; }

        [MaxLength(100)]
        public string? ExternalReference { get; set; }

        // ── Multi-Currency ──────────────────────────────────────────────
        [Column(TypeName = "varchar(10)")]
        public string? OriginalCurrency { get; set; }

        [Column(TypeName = "decimal(18,8)")]
        public decimal? ExchangeRate { get; set; }

        // ── Reversal & Disputes ─────────────────────────────────────────
        [Column(TypeName = "char(36)")]
        [MaxLength(36)]
        public string? ParentHistoryId { get; set; }

        [ForeignKey(nameof(ParentHistoryId))]
        public History? ParentHistory { get; set; }

        [MaxLength(255)]
        public string? ReversalReason { get; set; }

        [Column(TypeName = "varchar(30)")]
        public DisputeStatus? DisputeStatus { get; set; }

        [MaxLength(100)]
        public string? DisputeReference { get; set; }

        // ── Idempotency & Retry ─────────────────────────────────────────
        [MaxLength(100)]
        public string? IdempotencyKey { get; set; }

        public int RetryCount { get; set; } = 0;

        [MaxLength(500)]
        public string? FailureReason { get; set; }

        public DateTime? ProcessedAt { get; set; }

        // ── Channel & Device ────────────────────────────────────────────
        [Column(TypeName = "varchar(20)")]
        public TransactionChannel? Channel { get; set; }

        [MaxLength(100)]
        public string? DeviceId { get; set; }

        [MaxLength(255)]
        public string? UserAgent { get; set; }

        [MaxLength(100)]
        public string? GeoLocation { get; set; }

        // ── Compliance & Risk ───────────────────────────────────────────
        [Column(TypeName = "decimal(5,2)")]
        public decimal? RiskScore { get; set; }

        public bool AmlFlag { get; set; } = false;

        [MaxLength(50)]
        public string? SanctionScreeningResult { get; set; }

        [MaxLength(500)]
        public string? ComplianceNote { get; set; }

        [MaxLength(36)]
        public string? ReviewedBy { get; set; }

        // ── Admin Audit ─────────────────────────────────────────────────
        [MaxLength(36)]
        public string? InitiatedBy { get; set; }

        [MaxLength(36)]
        public string? ApprovedBy { get; set; }

        public DateTime? ApprovalTimestamp { get; set; }

        [MaxLength(500)]
        public string? AdminNote { get; set; }

        public bool ManualAdjustmentFlag { get; set; } = false;

        // ── Metadata ────────────────────────────────────────────────────
        [Column(TypeName = "varchar(30)")]
        public TransactionCategory? Category { get; set; }

        [Column(TypeName = "json")]
        public string? Tags { get; set; }
    }
}