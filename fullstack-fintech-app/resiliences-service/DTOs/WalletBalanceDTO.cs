using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace resiliences_service.DTOs
{
    public class WalletBalanceDTO
    {
        [JsonPropertyName("currencyCode")]
        public string CurrencyCode { get; set; } = string.Empty;

        [JsonPropertyName("currencySymbol")]
        public string CurrencySymbol { get; set; } = string.Empty;

        [JsonPropertyName("balance")]
        public decimal Balance { get; set; }
    }
}