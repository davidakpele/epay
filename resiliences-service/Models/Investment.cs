using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using resiliences_service.Enums;

namespace resiliences_service.Models
{
    [Table("Investments")]
    public class Investment
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public long Id { get; set; }

        /// <summary>The user who owns this investment.</summary>
        [Required]
        public long UserId { get; set; }

        /// <summary>The wallet ID that was debited for this investment.</summary>
        [Required]
        public long WalletId { get; set; }

        /// <summary>Currency of the investment (NGN, USD, etc.).</summary>
        [Required]
        [Column(TypeName = "varchar(10)")]
        public string CurrencyCode { get; set; } = "NGN";

        /// <summary>Principal amount invested.</summary>
        [Required]
        [Column(TypeName = "decimal(18,4)")]
        public decimal Principal { get; set; }

        /// <summary>Annual return rate in percent (e.g. 12.00 = 12%).</summary>
        [Column(TypeName = "decimal(5,2)")]
        public decimal ReturnRate { get; set; }

        /// <summary>Expected profit based on principal × rate × duration.</summary>
        [Column(TypeName = "decimal(18,4)")]
        public decimal ExpectedProfit { get; set; }

        /// <summary>Total expected payout = principal + expectedProfit.</summary>
        [Column(TypeName = "decimal(18,4)")]
        public decimal TotalPayout { get; set; }

        /// <summary>Duration tier selected by the user.</summary>
        [Required]
        [Column(TypeName = "varchar(20)")]
        public InvestmentDuration Duration { get; set; }

        /// <summary>Number of days corresponding to the duration.</summary>
        public int DurationDays { get; set; }

        /// <summary>When the investment was created / started.</summary>
        public DateTime StartDate { get; set; } = DateTime.UtcNow;

        /// <summary>When the investment matures and payout becomes due.</summary>
        public DateTime MaturityDate { get; set; }

        /// <summary>Current lifecycle state of the investment.</summary>
        [Required]
        [Column(TypeName = "varchar(20)")]
        public InvestmentStatus Status { get; set; } = InvestmentStatus.ACTIVE;

        /// <summary>When the payout was actually processed.</summary>
        public DateTime? PaidOutAt { get; set; }

        /// <summary>System reference for this investment transaction.</summary>
        [Column(TypeName = "varchar(50)")]
        public string? ReferenceId { get; set; }

        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public DateTime CreatedOn { get; set; } = DateTime.UtcNow;

        [DatabaseGenerated(DatabaseGeneratedOption.Computed)]
        public DateTime UpdatedOn { get; set; } = DateTime.UtcNow;
    }
}
