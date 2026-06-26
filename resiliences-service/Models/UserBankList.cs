using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace resiliences_service.Models
{
    [Table("UserBankLists")]
    [Index(nameof(AccountNumber), IsUnique = true)]
    public class UserBankList
    {
        [Key]
        public uint Id { get; set; }

        public string? BankCode { get; set; }

        public string? BankName { get; set; }

        public string? AccountHolderName { get; set; }

        [Required]
        public string AccountNumber { get; set; } = default!;

        public uint UserId { get; set; }

        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public DateTime CreatedOn { get; set; } = DateTime.UtcNow;

        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public DateTime UpdatedOn { get; set; } = DateTime.UtcNow;
    }
}