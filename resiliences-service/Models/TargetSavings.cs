using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using resiliences_service.Enums;

namespace resiliences_service.Models
{
    [Table("TargetSavings")]
    public class TargetSavings
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public long Id { get; set; }

        /// <summary>The user who created this savings goal.</summary>
        [Required]
        public long UserId { get; set; }

        /// <summary>The wallet ID linked to this savings goal.</summary>
        [Required]
        public long WalletId { get; set; }

        /// <summary>Currency used for this savings goal.</summary>
        [Required]
        [Column(TypeName = "varchar(10)")]
        public string CurrencyCode { get; set; } = "NGN";

        /// <summary>User-defined name for this savings goal (e.g. "Car", "House", "Vacation").</summary>
        [Required]
        [Column(TypeName = "varchar(100)")]
        public string GoalName { get; set; } = default!;

        /// <summary>Optional description or notes about this goal.</summary>
        [Column(TypeName = "varchar(500)")]
        public string? Description { get; set; }

        /// <summary>The total amount the user wants to save.</summary>
        [Required]
        [Column(TypeName = "decimal(18,4)")]
        public decimal TargetAmount { get; set; }

        /// <summary>Amount saved so far (accumulated from deposits to this goal).</summary>
        [Column(TypeName = "decimal(18,4)")]
        public decimal SavedAmount { get; set; } = 0;

        /// <summary>Optional target deadline date set by the user.</summary>
        public DateTime? TargetDate { get; set; }

        /// <summary>Current state of this savings goal.</summary>
        [Required]
        [Column(TypeName = "varchar(20)")]
        public TargetSavingsStatus Status { get; set; } = TargetSavingsStatus.ACTIVE;

        /// <summary>Emoji or icon identifier the user chose for this goal (optional).</summary>
        [Column(TypeName = "varchar(10)")]
        public string? GoalIcon { get; set; }

        /// <summary>When this savings goal was completed (reached target).</summary>
        public DateTime? CompletedAt { get; set; }

        /// <summary>When the user withdrew from this goal.</summary>
        public DateTime? WithdrawnAt { get; set; }

        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public DateTime CreatedOn { get; set; } = DateTime.UtcNow;

        [DatabaseGenerated(DatabaseGeneratedOption.Computed)]
        public DateTime UpdatedOn { get; set; } = DateTime.UtcNow;
    }
}
