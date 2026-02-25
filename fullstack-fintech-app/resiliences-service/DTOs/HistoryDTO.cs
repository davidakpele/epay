using System;

namespace resiliences_service.DTOs
{
    public class HistoryDTO
    {
        public string Id { get; set; } = default!;
        public ulong WalletId { get; set; }
        public ulong UserId { get; set; }
        public string? SessionId { get; set; }
        public double Amount { get; set; }
        public string? Type { get; set; }
        public string? Description { get; set; }
        public string? Message { get; set; }
        public string? CurrencyType { get; set; }
        public string? Status { get; set; }
        public string? IpAddress { get; set; }
        public string Timestamp { get; set; } = default!;
        public string CreatedOn { get; set; } = default!;
        public string UpdatedOn { get; set; } = default!;
    }
}