using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace resiliences_service.DTOs
{
    public class UserRecordDTO
    {
        public long Id { get; set; }
        public string? FirstName { get; set; }
        public string? LastName { get; set; }
        public string? Gender { get; set; }
        public string? Country { get; set; }
        public string? City { get; set; }
        public bool TransferPinSet { get; set; }
        public bool Locked { get; set; }
        public DateTime? LockedAt { get; set; }
        public string? ReferralCode { get; set; }
        public bool Blocked { get; set; }
        public long? BlockedDuration { get; set; }
        public string? BlockedUntil { get; set; }
        public string? BlockedReason { get; set; }
        public string? TotalReferers { get; set; }
        public string? ReferralLink { get; set; }
        public string? Photo { get; set; }
    }
}