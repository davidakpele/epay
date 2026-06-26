using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace resiliences_service.Payloads
{
    public class CreateBankPayload
    {
        [JsonPropertyName("bank_code")]
        public string BankCode { get; set; } = default!;

        [JsonPropertyName("bank_name")]
        public string BankName { get; set; } = default!;

        [JsonPropertyName("account_holder_name")]
        public string AccountHolderName { get; set; } = default!;

        [JsonPropertyName("account_number")]
        public string AccountNumber { get; set; } = default!;

        [JsonPropertyName("user_id")]
        public uint UserId { get; set; }
    }

    public class PayStackBankList
    {
        public int Id { get; set; }
        public string Name { get; set; } = default!;
        public string Slug { get; set; } = default!;
        public string Code { get; set; } = default!;
        public string Longcode { get; set; } = default!;
        public string Gateway { get; set; } = default!;
        public bool PayWithBank { get; set; }
        public bool SupportsTransfer { get; set; }
        public bool Active { get; set; }
        public string Country { get; set; } = default!;
        public string Currency { get; set; } = default!;
        public string Type { get; set; } = default!;
        public bool IsDeleted { get; set; }
        public string CreatedAt { get; set; } = default!;
        public string UpdatedAt { get; set; } = default!;
    }

    public class PaystackBankResponse
    {
        public bool Status { get; set; }
        public string Message { get; set; } = default!;
        public List<PayStackBankList> Data { get; set; } = new();
    }

    public class PaystackAccountData
    {
        [JsonPropertyName("account_number")]
        public string AccountNumber { get; set; } = default!;

        [JsonPropertyName("account_name")]
        public string AccountName { get; set; } = default!;

        [JsonPropertyName("bank_id")]
        public int BankId { get; set; }
    }

    public class PaystackAccountResponse
    {
        public bool Status { get; set; }
        public string Message { get; set; } = default!;
        public PaystackAccountData Data { get; set; } = default!;
    }
}