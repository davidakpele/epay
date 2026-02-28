using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using resiliences_service.Enums;

namespace resiliences_service.Models
{
    public class BlackListedWallet
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public uint Id { get; set; }

        [Required]
        public uint WalletId { get; set; }

        [Required]
        [Column(TypeName = "varchar(200)")]
        public BannedReasons BankBannedReason { get; set; }

        [Required]
        public bool IsBlock { get; set; }

        [Required]
        public DateTime Timestamp { get; set; }

        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [DatabaseGenerated(DatabaseGeneratedOption.Computed)]
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    }
}