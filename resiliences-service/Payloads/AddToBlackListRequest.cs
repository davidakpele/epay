using System.ComponentModel.DataAnnotations;
using resiliences_service.Enums;

namespace resiliences_service.Payloads
{
    public class AddToBlackListRequest
    {
        [Required]
        public uint WalletId { get; set; }

        [Required]
        public BannedReasons Reason { get; set; }

        [Required]
        public bool IsBlock { get; set; }
    }
}