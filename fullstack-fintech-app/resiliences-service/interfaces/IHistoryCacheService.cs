using resiliences_service.Models;

namespace resiliences_service.interfaces
{
    public interface IHistoryCacheService
    {
        Task CacheUserHistoriesAsync(ulong userId);
        Task<List<History>?> GetCachedUserHistoriesAsync(ulong userId);
        Task<List<History>?> GetCachedAllHistoriesAsync();
        Task InvalidateUserCacheAsync(ulong userId);
        Task InvalidateAllCacheAsync();
        Task<Dictionary<string, object>> GetCacheStatsAsync();
        Task<bool> HealthCheckAsync();
        void StartCacheWarmup(CancellationToken cancellationToken);
        bool IsWarmupInProgress();
    }
}