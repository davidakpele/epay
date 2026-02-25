using resiliences_service.Models;

namespace resiliences_service.interfaces
{
    public interface IHistoryService
    {
        Task<History> CreateWithdrawalAsync(History history);
        Task<History> CreateDepositAsync(History history);
        Task<History> CreateSwapAsync(History history);
        Task<History?> GetByIdAsync(string id);
        Task<List<History>> GetByUserIdAsync(ulong userId);
        Task<List<History>> GetByWalletIdAsync(ulong walletId);
        Task<List<History>> GetBySessionIdAsync(string sessionId);
        Task DeleteAsync(string id);
        Task<List<History>> GetByUserIdAndCurrencyAsync(ulong userId, string currency);
        Task<List<History>> GetByTimestampAfterAndWalletIdAsync(ulong walletId, DateTime timestamp);
        Task<List<History>> GetRecentByUserIdAsync(ulong userId, int minutes);
        Task<List<History>> GetByUserIdWithFiltersAsync(ulong userId, DateTime? startDate, DateTime? endDate, string? transactionType, string? currency);
        Task<List<History>?> GetCachedUserHistoriesAsync(ulong userId);
        Task<List<History>?> GetCachedAllHistoriesAsync();
        Task InvalidateUserCacheAsync(ulong userId);
        Task InvalidateAllCacheAsync();
    }
}