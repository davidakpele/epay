using System.ComponentModel.DataAnnotations;

namespace resiliences_service.Payloads
{
    public class TopUpTargetSavingsRequest
    {
        [Required]
        public long UserId { get; set; }

        [Required]
        public long WalletId { get; set; }

        [Required]
        [Range(0.01, double.MaxValue, ErrorMessage = "Amount must be greater than 0")]
        public decimal Amount { get; set; }
    }
}
