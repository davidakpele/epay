using System;
using System.Text.Json.Serialization;

namespace resiliences_service.DTOs
{
    public class HistoryDTO
    {
        // Core Identity
        [JsonPropertyName("id")]
        public string Id { get; set; } = string.Empty;

        [JsonPropertyName("walletId")]
        public long WalletId { get; set; }

        [JsonPropertyName("userId")]
        public long UserId { get; set; }

        [JsonPropertyName("sessionId")]
        public string SessionId { get; set; } = string.Empty;

        [JsonPropertyName("transactionId")]
        public string TransactionId { get; set; } = string.Empty;

        [JsonPropertyName("referenceNo")]
        public string ReferenceNo { get; set; } = string.Empty;

        [JsonPropertyName("terminalId")]
        public string TerminalId { get; set; } = string.Empty;

        [JsonPropertyName("erId")]
        public string ErId { get; set; } = string.Empty;

        [JsonPropertyName("accountHolder")]
        public string AccountHolder { get; set; } = string.Empty;

        // Financial Amounts
        [JsonPropertyName("previousBalance")]
        public decimal PreviousBalance { get; set; }

        [JsonPropertyName("availableBalance")]
        public decimal AvailableBalance { get; set; }

        [JsonPropertyName("amount")]
        public decimal Amount { get; set; }

        [JsonPropertyName("grossAmount")]
        public decimal GrossAmount { get; set; }

        [JsonPropertyName("feeAmount")]
        public decimal FeeAmount { get; set; }

        [JsonPropertyName("taxAmount")]
        public decimal TaxAmount { get; set; }

        [JsonPropertyName("netAmount")]
        public decimal NetAmount { get; set; }

        [JsonPropertyName("runningBalance")]
        public decimal RunningBalance { get; set; }

        // Transaction Details
        [JsonPropertyName("type")]
        public string TransactionType { get; set; } = string.Empty;

        [JsonPropertyName("description")]
        public string Description { get; set; } = string.Empty;

        [JsonPropertyName("message")]
        public string Message { get; set; } = string.Empty;

        [JsonPropertyName("currencyType")]
        public string CurrencyType { get; set; } = string.Empty;

        [JsonPropertyName("status")]
        public string Status { get; set; } = string.Empty;

        [JsonPropertyName("ipAddress")]
        public string IpAddress { get; set; } = string.Empty;

        [JsonPropertyName("timestamp")]
        public string Timestamp { get; set; } = string.Empty;

        // Double-Entry Accounting
        [JsonPropertyName("debitCredit")]
        public string DebitCredit { get; set; } = string.Empty;

        [JsonPropertyName("ledgerEntryType")]
        public string LedgerEntryType { get; set; } = string.Empty;

        // Counterparty & Routing
        [JsonPropertyName("counterpartyWalletId")]
        public long CounterpartyWalletId { get; set; }

        [JsonPropertyName("counterpartyUserId")]
        public long CounterpartyUserId { get; set; }

        [JsonPropertyName("counterpartyAccountHolder")]
        public string CounterpartyAccountHolder { get; set; } = string.Empty;

        [JsonPropertyName("bankCode")]
        public string BankCode { get; set; } = string.Empty;

        [JsonPropertyName("bankAccountNumber")]
        public string BankAccountNumber { get; set; } = string.Empty;

        [JsonPropertyName("routingNumber")]
        public string RoutingNumber { get; set; } = string.Empty;

        [JsonPropertyName("externalReference")]
        public string ExternalReference { get; set; } = string.Empty;

        // Multi-Currency
        [JsonPropertyName("originalCurrency")]
        public string OriginalCurrency { get; set; } = string.Empty;

        [JsonPropertyName("exchangeRate")]
        public decimal ExchangeRate { get; set; }

        // Reversal & Disputes
        [JsonPropertyName("parentHistoryId")]
        public string ParentHistoryId { get; set; } = string.Empty;

        [JsonPropertyName("reversalReason")]
        public string ReversalReason { get; set; } = string.Empty;

        [JsonPropertyName("disputeStatus")]
        public string DisputeStatus { get; set; } = string.Empty;

        [JsonPropertyName("disputeReference")]
        public string DisputeReference { get; set; } = string.Empty;

        // Idempotency & Retry
        [JsonPropertyName("idempotencyKey")]
        public string IdempotencyKey { get; set; } = string.Empty;

        [JsonPropertyName("retryCount")]
        public int RetryCount { get; set; }

        [JsonPropertyName("failureReason")]
        public string FailureReason { get; set; } = string.Empty;

        [JsonPropertyName("processedAt")]
        public string ProcessedAt { get; set; } = string.Empty;

        // Channel & Device
        [JsonPropertyName("channel")]
        public string Channel { get; set; } = string.Empty;

        [JsonPropertyName("deviceId")]
        public string DeviceId { get; set; } = string.Empty;

        [JsonPropertyName("userAgent")]
        public string UserAgent { get; set; } = string.Empty;

        [JsonPropertyName("geoLocation")]
        public string GeoLocation { get; set; } = string.Empty;

        // Compliance & Risk
        [JsonPropertyName("riskScore")]
        public decimal RiskScore { get; set; }

        [JsonPropertyName("amlFlag")]
        public bool AmlFlag { get; set; }

        [JsonPropertyName("sanctionScreeningResult")]
        public string SanctionScreeningResult { get; set; } = string.Empty;

        [JsonPropertyName("complianceNote")]
        public string ComplianceNote { get; set; } = string.Empty;

        [JsonPropertyName("reviewedBy")]
        public string ReviewedBy { get; set; } = string.Empty;

        // Admin Audit
        [JsonPropertyName("initiatedBy")]
        public string InitiatedBy { get; set; } = string.Empty;

        [JsonPropertyName("approvedBy")]
        public string ApprovedBy { get; set; } = string.Empty;

        [JsonPropertyName("approvalTimestamp")]
        public string ApprovalTimestamp { get; set; } = string.Empty;

        [JsonPropertyName("adminNote")]
        public string AdminNote { get; set; } = string.Empty;

        [JsonPropertyName("manualAdjustmentFlag")]
        public bool ManualAdjustmentFlag { get; set; }

        // Metadata
        [JsonPropertyName("category")]
        public string Category { get; set; } = string.Empty;

        [JsonPropertyName("tags")]
        public string Tags { get; set; } = string.Empty;

        // System Fields
        [JsonPropertyName("createdOn")]
        public string CreatedOn { get; set; } = string.Empty;

        [JsonPropertyName("updatedOn")]
        public string UpdatedOn { get; set; } = string.Empty;
    }
}