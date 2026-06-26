using System.ComponentModel.DataAnnotations;
using resiliences_service.Enums;

namespace resiliences_service.Payloads
{
    public class CalculateInvestmentRequest
    {
        [Required]
        [Range(0.01, double.MaxValue, ErrorMessage = "Principal must be greater than 0")]
        public decimal Principal { get; set; }

        [Required]
        public InvestmentDuration Duration { get; set; }

        [Required]
        [StringLength(10)]
        public string CurrencyCode { get; set; } = "NGN";
    }
}
