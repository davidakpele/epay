using Microsoft.EntityFrameworkCore;
using resiliences_service.Models;
using resiliences_service.Configs;
using resiliences_service.interfaces;

namespace resiliences_service.Repositories
{
    public class HistoryRepository : IHistoryRepository
    {
        private readonly AppDbContext _context;

        public HistoryRepository(AppDbContext context)
        {
            _context = context;
        }

        public async Task<History> CreateAsync(History history)
        {
            _context.Histories.Add(history);
            await _context.SaveChangesAsync();
            return history;
        }

        public async Task<History?> FindByIdAsync(string id)
        {
            return await _context.Histories.FirstOrDefaultAsync(h => h.Id == id);
        }

        public async Task<List<History>> FindByUserIdAsync(ulong userId)
        {
            return await _context.Histories
                .Where(h => h.UserId == userId)
                .OrderByDescending(h => h.CreatedOn)
                .ToListAsync();
        }

        public async Task<List<History>> FindByWalletIdAsync(ulong walletId)
        {
            return await _context.Histories
                .Where(h => h.WalletId == walletId)
                .OrderByDescending(h => h.CreatedOn)
                .ToListAsync();
        }

        public async Task<List<History>> FindBySessionIdAsync(string sessionId)
        {
            return await _context.Histories
                .Where(h => h.SessionId == sessionId)
                .OrderByDescending(h => h.CreatedOn)
                .ToListAsync();
        }

        public async Task<History> UpdateAsync(History history)
        {
            _context.Histories.Update(history);
            await _context.SaveChangesAsync();
            return history;
        }

        public async Task DeleteAsync(string id)
        {
            var history = await FindByIdAsync(id);
            if (history != null)
            {
                _context.Histories.Remove(history);
                await _context.SaveChangesAsync();
            }
        }

        public async Task<List<History>> FindByUserIdAndCurrencyAsync(ulong userId, string currency)
        {
            return await _context.Histories
                .Where(h => h.UserId == userId && h.CurrencyType == currency)
                .OrderByDescending(h => h.CreatedOn)
                .ToListAsync();
        }

        public async Task<List<History>> FindByTimestampAfterAndWalletIdAsync(ulong walletId, DateTime timestamp)
        {
            return await _context.Histories
                .Where(h => h.WalletId == walletId && h.Timestamp > timestamp)
                .OrderByDescending(h => h.Timestamp)
                .ToListAsync();
        }

        public async Task<List<History>> FindRecentByUserIdAsync(ulong userId, int minutes)
        {
            var since = DateTime.UtcNow.AddMinutes(-minutes);
            return await _context.Histories
                .Where(h => h.UserId == userId && h.CreatedOn >= since)
                .OrderByDescending(h => h.CreatedOn)
                .ToListAsync();
        }

        public async Task<List<History>> FindByUserIdWithFiltersAsync(ulong userId, DateTime? startDate, DateTime? endDate, string? transactionType, string? currency)
        {
            var query = _context.Histories.Where(h => h.UserId == userId);

            if (startDate.HasValue)
            {
                var start = DateTime.SpecifyKind(startDate.Value, DateTimeKind.Utc);
                query = query.Where(h => h.CreatedOn >= start);
            }

            if (endDate.HasValue)
            {
                var end = DateTime.SpecifyKind(endDate.Value.AddDays(1), DateTimeKind.Utc);
                query = query.Where(h => h.CreatedOn < end);
            }

            if (!string.IsNullOrEmpty(transactionType) && transactionType != "ALL")
                query = query.Where(h => h.Type.ToString() == transactionType);

            if (!string.IsNullOrEmpty(currency) && currency != "ALL")
                query = query.Where(h => h.CurrencyType == currency);

            if (transactionType == "ALL" && currency == "ALL")
                query = query.Take(100);

            return await query.OrderByDescending(h => h.CreatedOn).ToListAsync();
        }

        public async Task<List<History>> FindAllRecentAsync(DateTime since, int page, int pageSize)
        {
            return await _context.Histories
                .Where(h => h.Timestamp >= since)
                .OrderByDescending(h => h.Timestamp)
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .ToListAsync();
        }

        public async Task<List<ulong>> FindRecentActiveUserIdsAsync(DateTime since)
        {
            return await _context.Histories
                .Where(h => h.Timestamp >= since)
                .Select(h => h.UserId)
                .Distinct()
                .ToListAsync();
        }

        public async Task<long> CountAllAsync()
        {
            return await _context.Histories.LongCountAsync();
        }

        public async Task<List<AnalyticsDataPoint>> GetTransactionAnalyticsAsync(string period)
{
    var query = _context.Histories.AsQueryable();

    return period.ToUpper() switch
    {
        "DAILY" => (await query
            .Where(h => h.CreatedOn >= DateTime.UtcNow.AddDays(-30))
            .GroupBy(h => new { h.CreatedOn.Year, h.CreatedOn.Month, h.CreatedOn.Day })
            .Select(g => new
            {
                g.Key.Year,
                g.Key.Month,
                g.Key.Day,
                Count = g.LongCount(),
                Amount = g.Sum(h => h.NetAmount)  // Changed from h.Amount to h.NetAmount
            })
            .ToListAsync())
            .Select(g => new AnalyticsDataPoint
            {
                Label = new DateTime(g.Year, g.Month, g.Day).ToString("dd MMM"),
                Count = g.Count,
                Amount = g.Amount
            })
            .OrderBy(x => x.Label)
            .ToList(),

        "WEEKLY" => (await query
            .Where(h => h.CreatedOn >= DateTime.UtcNow.AddDays(-84))
            .GroupBy(h => new { h.CreatedOn.Year, h.CreatedOn.Month, h.CreatedOn.Day })
            .Select(g => new
            {
                g.Key.Year,
                g.Key.Month,
                g.Key.Day,
                Count = g.LongCount(),
                Amount = g.Sum(h => h.NetAmount)  // Changed from h.Amount to h.NetAmount
            })
            .ToListAsync())
            .Select(g =>
            {
                var date = new DateTime(g.Year, g.Month, g.Day);
                var week = System.Globalization.ISOWeek.GetWeekOfYear(date);
                return new AnalyticsDataPoint
                {
                    Label = $"Week {week} {g.Year}",
                    Count = g.Count,
                    Amount = g.Amount
                };
            })
            .GroupBy(x => x.Label)
            .Select(g => new AnalyticsDataPoint
            {
                Label = g.Key,
                Count = g.Sum(x => x.Count),
                Amount = g.Sum(x => x.Amount)
            })
            .OrderBy(x => x.Label)
            .ToList(),

        "MONTHLY" => (await query
            .Where(h => h.CreatedOn >= DateTime.UtcNow.AddMonths(-12))
            .GroupBy(h => new { h.CreatedOn.Year, h.CreatedOn.Month })
            .Select(g => new
            {
                g.Key.Year,
                g.Key.Month,
                Count = g.LongCount(),
                Amount = g.Sum(h => h.NetAmount)  // Changed from h.Amount to h.NetAmount
            })
            .ToListAsync())
            .Select(g => new AnalyticsDataPoint
            {
                Label = $"{g.Year}-{g.Month:D2}",
                Count = g.Count,
                Amount = g.Amount
            })
            .OrderBy(x => x.Label)
            .ToList(),

        "YEARLY" => (await query
            .GroupBy(h => h.CreatedOn.Year)
            .Select(g => new
            {
                Year = g.Key,
                Count = g.LongCount(),
                Amount = g.Sum(h => h.NetAmount) 
            })
            .ToListAsync())
            .Select(g => new AnalyticsDataPoint
            {
                Label = g.Year.ToString(),
                Count = g.Count,
                Amount = g.Amount
            })
            .OrderBy(x => x.Label)
            .ToList(),

        _ => throw new ArgumentException($"Invalid period: {period}")
    };
}


    }
}