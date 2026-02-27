using System.Text.Json.Serialization;
using resiliences_service.Enums;

namespace resiliences_service.Models
{
    public class WalletMaintenance
    {
        [JsonPropertyName("id")]
        public long Id { get; set; }

        [JsonPropertyName("userId")]
        public long UserId { get; set; }

        [JsonPropertyName("currencyType")]
        public CurrencyType CurrencyType { get; set; }

        [JsonPropertyName("balance")]
        public decimal Balance { get; set; }

        [JsonPropertyName("status")]
        public DebtStatus Status { get; set; }

        [JsonPropertyName("lastCharged")]
        public DateTime? LastCharged { get; set; }

        [JsonPropertyName("createdOn")]
        public DateTime? CreatedOn { get; set; }

        [JsonPropertyName("updatedOn")]
        public DateTime? UpdatedOn { get; set; }
    }
}