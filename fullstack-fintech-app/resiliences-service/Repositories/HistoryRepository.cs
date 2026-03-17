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
    }
}