using System;
using System.Text.Json.Serialization;

namespace resiliences_service.DTOs
{
    public class HistoryDTO
    {
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

        [JsonPropertyName("previousBalance")]
        public decimal PreviousBalance { get; set; }

        [JsonPropertyName("availableBalance")]
        public decimal AvailableBalance { get; set; }

        [JsonPropertyName("amount")]
        public decimal Amount { get; set; }

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
    }
}