using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using resiliences_service.Enums;

namespace resiliences_service.Payloads
{
    public class CreateBeneficiaryRequest
    {
        public uint UserId { get; set; }
        public BeneficiaryType BeneficiaryType { get; set; }
        public string BeneficiaryName { get; set; } = default!;
        public string Currency { get; set; } = "NGN";
        public string? AccountNumber { get; set; }
        public string? AccountName { get; set; }
        public string? BankCode { get; set; }
        public string? BankName { get; set; }
        public string? RecipientUsername { get; set; }
    }
}