using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace resiliences_service.Payloads
{
    public class DeleteBeneficiariesRequest
    {
        public uint UserId { get; set; }
        public List<uint> Ids { get; set; } = new();
    }
}