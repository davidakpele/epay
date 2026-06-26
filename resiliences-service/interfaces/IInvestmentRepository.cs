using resiliences_service.Enums;
using resiliences_service.Models;

namespace resiliences_service.interfaces
{
    public interface IInvestmentRepository
    {
        Task<Investment> CreateAsync(Investment investment);
        Task<Investment?> FindByIdAsync(long id);
        Task<List<Investment>> FindByUserIdAsync(long userId);
        Task<List<Investment>> FindByStatusAsync(InvestmentStatus status);
        Task<List<Investment>> FindMaturedUnpaidAsync();
        Task<Investment> UpdateAsync(Investment investment);
    }
}
