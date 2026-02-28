using resiliences_service.Models;
using resiliences_service.Enums;

namespace resiliences_service.interfaces
{
    public interface IHistoryRepository
    {
        Task<History> CreateAsync(History history);
        Task<History?> FindByIdAsync(string id);
        Task<List<History>> FindByUserIdAsync(ulong userId);
        Task<List<History>> FindByWalletIdAsync(ulong walletId);
        Task<List<History>> FindBySessionIdAsync(string sessionId);
        Task<History> UpdateAsync(History history);
        Task DeleteAsync(string id);
        Task<List<History>> FindByUserIdAndCurrencyAsync(ulong userId, string currency);
        Task<List<History>> FindByTimestampAfterAndWalletIdAsync(ulong walletId, DateTime timestamp);
        Task<List<History>> FindRecentByUserIdAsync(ulong userId, int minutes);
        Task<List<History>> FindByUserIdWithFiltersAsync(ulong userId, DateTime? startDate, DateTime? endDate, string? transactionType, string? currency);
        Task<List<History>> FindAllRecentAsync(DateTime since, int page, int pageSize);
        Task<List<ulong>> FindRecentActiveUserIdsAsync(DateTime since);
    }
}