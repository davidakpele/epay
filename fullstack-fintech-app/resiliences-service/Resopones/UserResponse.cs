using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using resiliences_service.DTOs;

namespace resiliences_service.Resopones
{
    public class UserResponse
    {
        [JsonPropertyName("success")]
        public bool Success { get; set; }

        [JsonPropertyName("message")]
        public string? Message { get; set; }

        [JsonPropertyName("data")]
        public List<UserDTO> Data { get; set; } = new();

        [JsonPropertyName("cached")]
        public bool Cached { get; set; }

        [JsonPropertyName("count")]
        public int Count { get; set; }
    }
}