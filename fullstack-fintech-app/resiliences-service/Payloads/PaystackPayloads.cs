using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace resiliences_service.Payloads
{
    public class CreateBankPayload
    {
        public string BankCode { get; set; } = default!;
        public string BankName { get; set; } = default!;
        public string AccountHolderName { get; set; } = default!;
        public string AccountNumber { get; set; } = default!;
        public uint UserId { get; set; }
    }

    public class PayStackBankList
    {
        public string Name { get; set; } = default!;
        public string Code { get; set; } = default!;
        public string Country { get; set; } = default!;
        public string Currency { get; set; } = default!;
    }

    public class PaystackBankResponse
    {
        public bool Status { get; set; }
        public string Message { get; set; } = default!;
        public List<PayStackBankList> Data { get; set; } = new();
    }

    public class PaystackAccountData
    {
        public string AccountNumber { get; set; } = default!;
        public string AccountName { get; set; } = default!;
        public int BankId { get; set; }
    }

    public class PaystackAccountResponse
    {
        public bool Status { get; set; }
        public string Message { get; set; } = default!;
        public PaystackAccountData Data { get; set; } = default!;
    }
}