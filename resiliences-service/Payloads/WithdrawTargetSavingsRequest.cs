using System.ComponentModel.DataAnnotations;

namespace resiliences_service.Payloads
{
    public class WithdrawTargetSavingsRequest
    {
        [Required]
        public long UserId { get; set; }

        [Required]
        public long WalletId { get; set; }
    }
}
