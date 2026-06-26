using resiliences_service.Enums;
using resiliences_service.Models;

namespace resiliences_service.interfaces
{
    public interface ITargetSavingsRepository
    {
        Task<TargetSavings> CreateAsync(TargetSavings savings);
        Task<TargetSavings?> FindByIdAsync(long id);
        Task<TargetSavings?> FindByIdAndUserIdAsync(long id, long userId);
        Task<List<TargetSavings>> FindByUserIdAsync(long userId);
        Task<List<TargetSavings>> FindByUserIdAndStatusAsync(long userId, TargetSavingsStatus status);
        Task<TargetSavings> UpdateAsync(TargetSavings savings);
    }
}
