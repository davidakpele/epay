
using System.Text.Json.Serialization;
using resiliences_service.Enums;

namespace resiliences_service.Models
{
    public class DebtCollector
    {
        [JsonPropertyName("id")]
        public long Id { get; set; }

        [JsonPropertyName("userId")]
        public long UserId { get; set; }

        [JsonPropertyName("amount")]
        public decimal Amount { get; set; }

        [JsonPropertyName("dueAmount")]
        public decimal DueAmount { get; set; }

        [JsonPropertyName("debtStatus")]
        public DebtStatus? DebtStatus { get; set; }

        [JsonPropertyName("description")]
        public string? Description { get; set; }

        [JsonPropertyName("currencyType")]
        public CurrencyType CurrencyType { get; set; }

        [JsonPropertyName("createdOn")]
        public DateTime? CreatedOn { get; set; }

        [JsonPropertyName("updatedOn")]
        public DateTime? UpdatedOn { get; set; }
    }
}