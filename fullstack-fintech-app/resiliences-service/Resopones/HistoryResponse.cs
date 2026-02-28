using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using resiliences_service.DTOs;

namespace resiliences_service.Resopones
{
    public class HistoryResponse
    {
        [JsonPropertyName("cached")]
        public bool Cached { get; set; }

        [JsonPropertyName("count")]
        public int Count { get; set; }

        [JsonPropertyName("data")]
        public List<HistoryDTO> Data { get; set; } = new();

        [JsonPropertyName("source")]
        public string Source { get; set; } = string.Empty;
    }
}