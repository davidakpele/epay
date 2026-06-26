using System.ComponentModel.DataAnnotations;

namespace resiliences_service.Payloads
{
    public class CreateTargetSavingsRequest
    {
        [Required]
        public long UserId { get; set; }

        [Required]
        public long WalletId { get; set; }

        [Required]
        [StringLength(10)]
        public string CurrencyCode { get; set; } = "NGN";

        [Required]
        [StringLength(100, MinimumLength = 1)]
        public string GoalName { get; set; } = default!;

        [StringLength(500)]
        public string? Description { get; set; }

        [Required]
        [Range(0.01, double.MaxValue, ErrorMessage = "Target amount must be greater than 0")]
        public decimal TargetAmount { get; set; }

        public DateTime? TargetDate { get; set; }

        [StringLength(10)]
        public string? GoalIcon { get; set; }
    }
}
