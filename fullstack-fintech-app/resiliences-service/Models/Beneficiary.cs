using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;
using resiliences_service.Enums;

namespace resiliences_service.Models
{
    [Table("beneficiaries")]
    [Index(nameof(UserId), nameof(BeneficiaryType), Name = "idx_user_beneficiary")]
    [Index(nameof(AccountNumber), Name = "idx_account_number")]
    [Index(nameof(RecipientUsername), Name = "idx_recipient_username")]
    public class Beneficiary
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public uint Id { get; set; }

        [Required]
        public uint UserId { get; set; }

        [Required]
        [Column(TypeName = "varchar(20)")]
        public BeneficiaryType BeneficiaryType { get; set; }

        [Required]
        [Column(TypeName = "varchar(255)")]
        public string BeneficiaryName { get; set; } = default!;

        [Required]
        [Column(TypeName = "varchar(10)")]
        public string Currency { get; set; } = "NGN";

        // Bank-specific fields
        [Column(TypeName = "varchar(20)")]
        public string? AccountNumber { get; set; }

        [Column(TypeName = "varchar(255)")]
        public string? AccountName { get; set; }

        [Column(TypeName = "varchar(50)")]
        public string? BankCode { get; set; }

        [Column(TypeName = "varchar(255)")]
        public string? BankName { get; set; }

        // User transfer-specific field
        [Column(TypeName = "varchar(255)")]
        public string? RecipientUsername { get; set; }

        public bool IsActive { get; set; } = true;

        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public DateTime CreatedOn { get; set; } = DateTime.UtcNow;

        [DatabaseGenerated(DatabaseGeneratedOption.Computed)]
        public DateTime UpdatedOn { get; set; } = DateTime.UtcNow;
    }
}