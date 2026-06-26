using resiliences_service.Models;
using resiliences_service.Payloads;

namespace resiliences_service.interfaces
{
    public interface ITargetSavingsService
    {
        /// <summary>Create a new savings goal (no initial deposit).</summary>
        Task<TargetSavings> CreateGoalAsync(CreateTargetSavingsRequest request);

        /// <summary>Deposit money from wallet into a savings goal.</summary>
        Task<TargetSavings> TopUpAsync(long savingsId, TopUpTargetSavingsRequest request);

        /// <summary>Withdraw all saved funds back to the main wallet.</summary>
        Task<TargetSavings> WithdrawAsync(long savingsId, WithdrawTargetSavingsRequest request);

        /// <summary>Get a single savings goal by ID (user-scoped).</summary>
        Task<TargetSavings?> GetByIdAsync(long id, long userId);

        /// <summary>Get all savings goals for a user.</summary>
        Task<List<TargetSavings>> GetByUserIdAsync(long userId);

        /// <summary>Cancel a savings goal (no funds withdrawn — must call Withdraw first).</summary>
        Task CancelAsync(long savingsId, long userId);
    }
}
