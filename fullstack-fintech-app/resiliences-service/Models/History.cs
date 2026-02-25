using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using resiliences_service.Enums;

namespace resiliences_service.Models
{
    [Table("Histories")]
    public class History
    {
        [Key]
        [Column(TypeName = "char(36)")]
        [MaxLength(36)]
        public string Id { get; set; } = Guid.NewGuid().ToString();

        [Required]
        public ulong WalletId { get; set; }

        [Required]
        public ulong UserId { get; set; }

        public string? SessionId { get; set; }

        public string? TransactionId { get; set; }

        [Column("referenceNo")]
        public string? ReferenceId { get; set; }

        public string? TerminalId { get; set; }

        public string? ErId { get; set; }

        public string? AccountHolder { get; set; }

        public double PreviousBalance { get; set; }

        public double AvailableBalance { get; set; }

        public double Amount { get; set; }

        [Column(TypeName = "varchar(50)")]
        public TransactionType? Type { get; set; }

        public string? Description { get; set; }

        public string? Message { get; set; }

        [Required]
        [Column(TypeName = "varchar(10)")]
        public string CurrencyType { get; set; } = default!;

        public string? Status { get; set; }

        [Column("ip_address")]
        [MaxLength(45)]
        public string? IpAddress { get; set; }

        public DateTime? Timestamp { get; set; }

        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public DateTime CreatedOn { get; set; } = DateTime.UtcNow;

        // Change from Computed to Identity, and set default
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public DateTime UpdatedOn { get; set; } = DateTime.UtcNow;


    }
}