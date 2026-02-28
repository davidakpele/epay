using System.Text.Json;
using Microsoft.Extensions.Caching.Distributed;
using resiliences_service.Models;
using resiliences_service.interfaces;
using StackExchange.Redis;

namespace resiliences_service.Services
{
    public class HistoryCacheService : IHistoryCacheService
    {
        private const string AllHistoriesKey     = "histories:all";
        private const string UserHistoriesKeyFmt = "histories:user:{0}";
        private const int    DefaultPageSize     = 500;
        private const long   MaxCacheSizeBytes   = 10 * 1024 * 1024;

        private readonly IDistributedCache        _cache;
        private readonly IServiceScopeFactory     _scopeFactory;
        private readonly IConnectionMultiplexer   _redis;
        private readonly ILogger<HistoryCacheService> _logger;

        private volatile bool _warmupInProgress;
        private readonly SemaphoreSlim _warmupLock = new(1, 1);

        public HistoryCacheService(
            IDistributedCache cache,
            IServiceScopeFactory scopeFactory,
            IConnectionMultiplexer redis,
            ILogger<HistoryCacheService> logger)
        {
            _cache        = cache;
            _scopeFactory = scopeFactory;
            _redis        = redis;
            _logger       = logger;
        }

        private IHistoryRepository GetRepo(IServiceScope scope) =>
            scope.ServiceProvider.GetRequiredService<IHistoryRepository>();

        public bool IsWarmupInProgress() => _warmupInProgress;

        public void StartCacheWarmup(CancellationToken cancellationToken)
        {
            _ = Task.Run(async () =>
            {
                _logger.LogInformation("[CacheWarmup] Starting warmup service");
                await WarmupCacheAsync();

                using var timer = new PeriodicTimer(TimeSpan.FromMinutes(10));
                while (await timer.WaitForNextTickAsync(cancellationToken))
                {
                    await WarmupCacheAsync();
                }
            }, cancellationToken);
        }

        private async Task WarmupCacheAsync()
        {
            if (!await _warmupLock.WaitAsync(0))
            {
                _logger.LogWarning("[CacheWarmup] Already in progress, skipping");
                return;
            }

            _warmupInProgress = true;
            try
            {
                _logger.LogInformation("[CacheWarmup] Starting");
                var start = DateTime.UtcNow;

                await CacheAllHistoriesAsync();
                await CacheRecentActiveUsersAsync();

                _logger.LogInformation("[CacheWarmup] Completed in {Ms}ms",
                    (DateTime.UtcNow - start).TotalMilliseconds);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "[CacheWarmup] Failed");
            }
            finally
            {
                _warmupInProgress = false;
                _warmupLock.Release();
            }
        }

        private async Task CacheAllHistoriesAsync()
        {
            using var scope = _scopeFactory.CreateScope();
            var repo = GetRepo(scope);

            var since = DateTime.UtcNow.AddDays(-30);
            var allHistories = new List<History>();
            var page = 1;

            while (page <= 50)
            {
                var batch = await repo.FindAllRecentAsync(since, page, DefaultPageSize);
                if (batch.Count == 0) break;

                allHistories.AddRange(batch);

                if (allHistories.Count * 500 > MaxCacheSizeBytes)
                {
                    _logger.LogWarning("[CacheWarmup] Max cache size reached, truncating");
                    break;
                }

                if (batch.Count < DefaultPageSize) break;
                page++;
            }

            await SetCacheAsync(AllHistoriesKey, allHistories);
            _logger.LogInformation("[CacheWarmup] Cached {Count} histories", allHistories.Count);
        }

        private async Task CacheRecentActiveUsersAsync()
        {
            using var scope = _scopeFactory.CreateScope();
            var repo = GetRepo(scope);

            var since = DateTime.UtcNow.AddDays(-7);
            var userIds = await repo.FindRecentActiveUserIdsAsync(since);

            _logger.LogInformation("[CacheWarmup] Caching histories for {Count} active users", userIds.Count);

            var semaphore = new SemaphoreSlim(5);
            var tasks = userIds.Select(async userId =>
            {
                await semaphore.WaitAsync();
                try { await CacheUserHistoriesAsync(userId); }
                catch (Exception ex) { _logger.LogWarning(ex, "[CacheWarmup] Failed for user {UserId}", userId); }
                finally { semaphore.Release(); }
            });

            await Task.WhenAll(tasks);
        }

        public async Task CacheUserHistoriesAsync(ulong userId)
        {
            using var scope = _scopeFactory.CreateScope();
            var repo = GetRepo(scope);

            var histories = await repo.FindByUserIdAsync(userId);
            var key = string.Format(UserHistoriesKeyFmt, userId);
            await SetCacheAsync(key, histories, TimeSpan.Zero);
        }

        public async Task<List<History>?> GetCachedUserHistoriesAsync(ulong userId)
        {
            var key = string.Format(UserHistoriesKeyFmt, userId);
            return await GetCacheAsync<List<History>>(key);
        }

        public async Task<List<History>?> GetCachedAllHistoriesAsync()
        {
            return await GetCacheAsync<List<History>>(AllHistoriesKey);
        }

        public async Task InvalidateUserCacheAsync(ulong userId)
        {
            var key = string.Format(UserHistoriesKeyFmt, userId);
            await _cache.RemoveAsync(key);
            _logger.LogInformation("[Cache] Invalidated user cache: {Key}", key);
        }

        public async Task InvalidateAllCacheAsync()
        {
            var db     = _redis.GetDatabase();
            var server = _redis.GetServers().First();
            var keys   = server.Keys(pattern: "resiliences_service:histories:*").ToArray();

            if (keys.Length > 0)
                await db.KeyDeleteAsync(keys);

            _logger.LogInformation("[Cache] Invalidated {Count} keys", keys.Length);
        }

        public async Task<bool> HealthCheckAsync()
        {
            try
            {
                await _cache.GetStringAsync("health_check");
                return true;
            }
            catch { return false; }
        }

        public async Task<Dictionary<string, object>> GetCacheStatsAsync()
        {
            var server = _redis.GetServers().First();
            var keys   = server.Keys(pattern: "resiliences_service:histories:*").Count();

            return new Dictionary<string, object>
            {
                ["cached_keys_count"]  = keys,
                ["warmup_in_progress"] = _warmupInProgress,
                ["instance_name"]      = "resiliences_service:"
            };
        }

        private async Task SetCacheAsync<T>(string key, T value, TimeSpan? expiry = null)
        {
            var json = JsonSerializer.Serialize(value);
            if (json.Length > MaxCacheSizeBytes)
            {
                _logger.LogWarning("[Cache] Data too large for key {Key}: {Size} bytes", key, json.Length);
                return;
            }

            var options = new DistributedCacheEntryOptions();
            if (expiry.HasValue && expiry.Value != TimeSpan.Zero)
                options.AbsoluteExpirationRelativeToNow = expiry;

            await _cache.SetStringAsync(key, json, options);
        }

        private async Task<T?> GetCacheAsync<T>(string key)
        {
            var json = await _cache.GetStringAsync(key);
            if (json == null) return default;
            return JsonSerializer.Deserialize<T>(json);
        }
    }
}