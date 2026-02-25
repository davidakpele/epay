using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using resiliences_service.interfaces;
using resiliences_service.Models;

namespace resiliences_service.Controllers
{
    [ApiController]
    [Route("api")]
    [Authorize]
    public class HistoryController : ControllerBase
    {
        private readonly IHistoryService _historyService;
        private readonly IHistoryCacheService _cacheService;
        private readonly ILogger<HistoryController> _logger;

        public HistoryController(IHistoryService historyService, IHistoryCacheService cacheService, ILogger<HistoryController> logger)
        {
            _historyService = historyService;
            _cacheService   = cacheService;
            _logger         = logger;
        }

        [HttpPost("history/withdrawal")]
        public async Task<IActionResult> CreateWithdrawal([FromBody] History request)
        {
            if (!ModelState.IsValid) return BadRequest(new { error = "Invalid input" });
            try { return StatusCode(201, await _historyService.CreateWithdrawalAsync(request)); }
            catch (Exception ex) { _logger.LogError(ex, "[HistoryController] CreateWithdrawal failed"); return StatusCode(500, new { error = "Failed to create withdrawal history" }); }
        }

        [HttpPost("history/deposit")]
        public async Task<IActionResult> CreateDeposit([FromBody] History request)
        {
            if (!ModelState.IsValid) return BadRequest(new { error = "Invalid input" });
            try { return StatusCode(201, await _historyService.CreateDepositAsync(request)); }
            catch (Exception ex) { _logger.LogError(ex, "[HistoryController] CreateDeposit failed"); return StatusCode(500, new { error = "Failed to create deposit history" }); }
        }

        [HttpPost("history/swap")]
        public async Task<IActionResult> CreateSwap([FromBody] History request)
        {
            if (!ModelState.IsValid) return BadRequest(new { error = "Invalid input" });
            try { return StatusCode(201, await _historyService.CreateSwapAsync(request)); }
            catch (Exception ex) { _logger.LogError(ex, "[HistoryController] CreateSwap failed"); return StatusCode(500, new { error = "Failed to create swap history" }); }
        }

        [HttpGet("history/{id}")]
        public async Task<IActionResult> GetById(string id)
        {
            var history = await _historyService.GetByIdAsync(id);
            return history == null ? NotFound(new { error = "History not found" }) : Ok(history);
        }

        [HttpDelete("history/{id}")]
        public async Task<IActionResult> Delete(string id)
        {
            await _historyService.DeleteAsync(id);
            return Ok(new { message = "History deleted successfully" });
        }

        [HttpGet("history/user/{userId}")]
        public async Task<IActionResult> GetByUserId(ulong userId)
        {
            var histories = await _historyService.GetByUserIdAsync(userId);
            return Ok(histories);
        }

        [HttpGet("history/wallet/{walletId}")]
        public async Task<IActionResult> GetByWalletId(ulong walletId)
        {
            var histories = await _historyService.GetByWalletIdAsync(walletId);
            return Ok(histories);
        }

        [HttpGet("history/session/{sessionId}")]
        public async Task<IActionResult> GetBySessionId(string sessionId)
        {
            var histories = await _historyService.GetBySessionIdAsync(sessionId);
            return Ok(histories);
        }

        [HttpGet("history/user/{userId}/currency/{currency}")]
        public async Task<IActionResult> GetByUserIdAndCurrency(ulong userId, string currency)
        {
            var histories = await _historyService.GetByUserIdAndCurrencyAsync(userId, currency);
            return Ok(histories);
        }

        [HttpGet("history/wallet/{walletId}/after")]
        public async Task<IActionResult> GetByTimestampAfterAndWalletId(ulong walletId, [FromQuery] DateTime timestamp)
        {
            var histories = await _historyService.GetByTimestampAfterAndWalletIdAsync(walletId, timestamp);
            return Ok(histories);
        }

        [HttpGet("history/user/{userId}/recent")]
        public async Task<IActionResult> GetRecentByUserId(ulong userId, [FromQuery] int minutes = 60)
        {
            var histories = await _historyService.GetRecentByUserIdAsync(userId, minutes);
            return Ok(histories);
        }

        [HttpGet("history/user/{userId}/filter")]
        public async Task<IActionResult> GetByUserIdWithFilters(
            ulong userId,
            [FromQuery] DateTime? fromDate,
            [FromQuery] DateTime? toDate,
            [FromQuery] string? transactionType,
            [FromQuery] string? currency)
        {
            var histories = await _historyService.GetByUserIdWithFiltersAsync(userId, fromDate, toDate, transactionType, currency);
            return Ok(new { data = histories });
        }

        // ── Cache endpoints ──

        [HttpGet("history/cache/user/{userId}")]
        public async Task<IActionResult> GetCachedUserHistories(ulong userId)
        {
            var cached = await _historyService.GetCachedUserHistoriesAsync(userId);
            if (cached != null)
                return Ok(new { data = cached, cached = true, count = cached.Count, source = "redis_cache" });

            var histories = await _historyService.GetByUserIdAsync(userId);
            _ = Task.Run(() => _cacheService.CacheUserHistoriesAsync(userId));
            return Ok(new { data = histories, cached = false, count = histories.Count, source = "database" });
        }

        [HttpGet("history/cache/all")]
        public async Task<IActionResult> GetCachedAllHistories()
        {
            var cached = await _historyService.GetCachedAllHistoriesAsync();
            if (cached != null)
                return Ok(new { data = cached, cached = true, count = cached.Count, source = "redis_cache" });

            return Ok(new { data = new List<History>(), cached = false, count = 0, source = "cache_miss", message = "Cache will be populated by scheduled job." });
        }

        [HttpPost("history/cache/refresh")]
        public IActionResult RefreshCache()
        {
            _cacheService.StartCacheWarmup(CancellationToken.None);
            return Accepted(new { message = "Cache refresh initiated", warmup_in_progress = _cacheService.IsWarmupInProgress() });
        }

        [HttpDelete("history/cache/user/{userId}")]
        public async Task<IActionResult> InvalidateUserCache(ulong userId)
        {
            await _historyService.InvalidateUserCacheAsync(userId);
            return Ok(new { message = "User cache invalidated", user_id = userId });
        }

        [HttpDelete("history/cache/all")]
        public async Task<IActionResult> InvalidateAllCache()
        {
            await _historyService.InvalidateAllCacheAsync();
            return Ok(new { message = "All history caches invalidated" });
        }

        [HttpGet("history/cache/stats")]
        public async Task<IActionResult> GetCacheStats()
        {
            var stats = await _cacheService.GetCacheStatsAsync();
            return Ok(new { stats });
        }

        [HttpGet("history/cache/health")]
        public async Task<IActionResult> CacheHealthCheck()
        {
            var healthy = await _cacheService.HealthCheckAsync();
            return healthy
                ? Ok(new { status = "healthy", service = "cache" })
                : StatusCode(503, new { status = "unhealthy", service = "cache" });
        }
    }
}