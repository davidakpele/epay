using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace resiliences_service.Resopones
{
    public class WalletResponse
    {
        [JsonPropertyName("wallet")]
        public WalletData Wallet { get; set; } = new();
    }
}