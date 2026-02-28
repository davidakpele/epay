using System.Text.Json.Serialization;
using resiliences_service.Enums;

namespace resiliences_service.Models
{
    public class MaintenanceFeeHistory
    {
        [JsonPropertyName("id")]
        public long Id { get; set; }

        [JsonPropertyName("userId")]
        public long UserId { get; set; }

        [JsonPropertyName("currencyType")]
        public CurrencyType CurrencyType { get; set; }

        [JsonPropertyName("feeAmount")]
        public decimal FeeAmount { get; set; }

        [JsonPropertyName("status")]
        public DebtStatus Status { get; set; }

        [JsonPropertyName("attemptedOn")]
        public DateTime? AttemptedOn { get; set; }

        [JsonPropertyName("paidOn")]
        public DateTime? PaidOn { get; set; }

        [JsonPropertyName("reason")]
        public string? Reason { get; set; }
    }
}