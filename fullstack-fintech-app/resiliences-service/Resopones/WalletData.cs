using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using resiliences_service.DTOs;

namespace resiliences_service.Resopones
{
    public class WalletData
    {
        [JsonPropertyName("id")]
        public long Id { get; set; }

        [JsonPropertyName("userId")]
        public long UserId { get; set; }

        [JsonPropertyName("balances")]
        public List<WalletBalanceDTO> Balances { get; set; } = new();

        [JsonPropertyName("createdOn")]
        public string CreatedOn { get; set; } = string.Empty;
    }
}