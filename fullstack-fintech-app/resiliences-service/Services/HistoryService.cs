using resiliences_service.Models;
using resiliences_service.interfaces;
using resiliences_service.Enums;

namespace resiliences_service.Services
{
    public class HistoryService : IHistoryService
    {
        private readonly IHistoryRepository     _repo;
        private readonly IHistoryCacheService   _cache;
        private readonly ILogger<HistoryService> _logger;

        public HistoryService(IHistoryRepository repo, IHistoryCacheService cache, ILogger<HistoryService> logger)
        {
            _repo   = repo;
            _cache  = cache;
            _logger = logger;
        }

        private void AutoFill(History history)
        {
            history.TransactionId ??= GenerateId("TXN");
            history.SessionId     ??= GenerateId("SESS");
            history.ReferenceId   ??= GenerateId("REF");
            history.TerminalId    ??= GenerateId("TERM");
            history.ErId          ??= GenerateId("ER");
            history.Timestamp     ??= DateTime.UtcNow;
            history.Status        ??= "SUCCESS";
        }

        private static string GenerateId(string prefix) =>
            $"{prefix}_{Guid.NewGuid():N}"[..20].ToUpper();

        private void InvalidateCacheFireAndForget(ulong userId)
        {
            _ = Task.Run(async () =>
            {
                try { await _cache.InvalidateUserCacheAsync(userId); }
                catch (Exception ex) { _logger.LogError(ex, "[HistoryService] Cache invalidation failed for userId: {UserId}", userId); }
            });
        }

        public async Task<History> CreateWithdrawalAsync(History history)
        {
            var created = await _repo.CreateAsync(history);
            InvalidateCacheFireAndForget(history.UserId);
            return created;
        }

        public async Task<History> CreateCreditAsync(History history)
        {
            history.Type = TransactionType.CREDITED;
            var created = await _repo.CreateAsync(history);
            InvalidateCacheFireAndForget(history.UserId);
            return created;
        }

        public async Task<History> CreateDepositAsync(History history)
        {
            AutoFill(history);
            history.Type = TransactionType.DEPOSIT;
            var created = await _repo.CreateAsync(history);
            InvalidateCacheFireAndForget(history.UserId);
            return created;
        }

        public async Task<History> CreateSwapAsync(History history)
        {
            AutoFill(history);
            history.Type = TransactionType.SWAP;
            var created = await _repo.CreateAsync(history);
            InvalidateCacheFireAndForget(history.UserId);
            return created;
        }

        public async Task<History?> GetByIdAsync(string id) =>
            await _repo.FindByIdAsync(id);

        public async Task<List<History>> GetByUserIdAsync(ulong userId) =>
            await _repo.FindByUserIdAsync(userId);

        public async Task<List<History>> GetByWalletIdAsync(ulong walletId) =>
            await _repo.FindByWalletIdAsync(walletId);

        public async Task<List<History>> GetBySessionIdAsync(string sessionId) =>
            await _repo.FindBySessionIdAsync(sessionId);

        public async Task DeleteAsync(string id) =>
            await _repo.DeleteAsync(id);

        public async Task<List<History>> GetByUserIdAndCurrencyAsync(ulong userId, string currency) =>
            await _repo.FindByUserIdAndCurrencyAsync(userId, currency);

        public async Task<List<History>> GetByTimestampAfterAndWalletIdAsync(ulong walletId, DateTime timestamp) =>
            await _repo.FindByTimestampAfterAndWalletIdAsync(walletId, timestamp);

        public async Task<List<History>> GetRecentByUserIdAsync(ulong userId, int minutes) =>
            await _repo.FindRecentByUserIdAsync(userId, minutes);

        public async Task<List<History>> GetByUserIdWithFiltersAsync(ulong userId, DateTime? startDate, DateTime? endDate, string? transactionType, string? currency) =>
            await _repo.FindByUserIdWithFiltersAsync(userId, startDate, endDate, transactionType, currency);

        public async Task<List<History>?> GetCachedUserHistoriesAsync(ulong userId) =>
            await _cache.GetCachedUserHistoriesAsync(userId);

        public async Task<List<History>?> GetCachedAllHistoriesAsync() =>
            await _cache.GetCachedAllHistoriesAsync();

        public async Task InvalidateUserCacheAsync(ulong userId) =>
            await _cache.InvalidateUserCacheAsync(userId);

        public async Task InvalidateAllCacheAsync() =>
            await _cache.InvalidateAllCacheAsync();

        public async Task<long> GetTotalCountAsync()
        {
            return await _repo.CountAllAsync();
        }

        public async Task<List<AnalyticsDataPoint>> GetTransactionAnalyticsAsync(string period)
        {
            return await _repo.GetTransactionAnalyticsAsync(period);
        }

        public async Task<List<History>> GetRecentAsync(int limit)
        {
            return await _repo.GetRecentAsync(limit);
        }
    }
}