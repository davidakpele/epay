using resiliences_service.Enums;
using resiliences_service.Models;
using resiliences_service.Payloads;

namespace resiliences_service.interfaces
{
    public interface IInvestmentService
    {
        /// <summary>Calculate and preview investment returns before committing.</summary>
        Task<object> CalculateReturnsAsync(decimal principal, InvestmentDuration duration, string currencyCode);

        /// <summary>Create a new investment: debit wallet, lock funds.</summary>
        Task<Investment> CreateInvestmentAsync(CreateInvestmentRequest request);

        /// <summary>Retrieve a single investment by ID (user-scoped).</summary>
        Task<Investment?> GetByIdAsync(long id, long userId);

        /// <summary>Retrieve all investments for a user.</summary>
        Task<List<Investment>> GetByUserIdAsync(long userId);

        /// <summary>Process payout for all matured-but-unpaid investments (called by worker).</summary>
        Task ProcessMaturedInvestmentsAsync();
    }
}
