using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using resiliences_service.Configs;
using resiliences_service.Enums;
using resiliences_service.interfaces;
using resiliences_service.Models;

namespace resiliences_service.Repositories
{
    public class WalletMaintenanceRepository : IWalletMaintenanceRepository
    {
        private readonly AppDbContext _db;

        public WalletMaintenanceRepository(AppDbContext db)
        {
            _db = db;
        }

        public async Task InitializeOrUpdateAsync(long userId, CurrencyType currencyType, decimal balance)
        {
            var existing = await _db.WalletMaintenances
                .FirstOrDefaultAsync(w => w.UserId == userId && w.CurrencyType == currencyType);

            if (existing == null)
            {
                _db.WalletMaintenances.Add(new WalletMaintenance
                {
                    UserId       = userId,
                    CurrencyType = currencyType,
                    Balance      = balance,
                    Status       = DebtStatus.PENDING,
                    CreatedOn    = DateTime.UtcNow,
                    UpdatedOn    = DateTime.UtcNow
                });
            }
            else
            {
                existing.Balance   = balance;
                existing.UpdatedOn = DateTime.UtcNow;
            }

            await _db.SaveChangesAsync();
        }

        public async Task DeductFeeAsync(long userId, CurrencyType currencyType, decimal amount)
        {
            var record = await _db.WalletMaintenances
                .FirstOrDefaultAsync(w => w.UserId == userId && w.CurrencyType == currencyType);

            if (record == null) return;

            record.Balance    -= amount;
            record.Status      = DebtStatus.PAID;
            record.LastCharged = DateTime.UtcNow;
            record.UpdatedOn   = DateTime.UtcNow;

            await _db.SaveChangesAsync();
        }

        public async Task MarkOverdueAsync(long userId, CurrencyType currencyType)
        {
            var record = await _db.WalletMaintenances
                .FirstOrDefaultAsync(w => w.UserId == userId && w.CurrencyType == currencyType);

            if (record == null) return;

            record.Status    = DebtStatus.OVERDUE;
            record.UpdatedOn = DateTime.UtcNow;

            await _db.SaveChangesAsync();
        }

        public async Task<DateTime?> GetLastChargedDateAsync(long userId, CurrencyType currencyType)
        {
            var record = await _db.WalletMaintenances
                .AsNoTracking()
                .FirstOrDefaultAsync(w => w.UserId == userId && w.CurrencyType == currencyType);

            return record?.LastCharged;
        }

        public async Task<(List<WalletMaintenance> Items, long Total)> GetAllPaginatedAsync(int page, int pageSize)
        {
            page     = Math.Max(page, 1);
            pageSize = Math.Clamp(pageSize, 1, 100);
            var offset = (page - 1) * pageSize;

            var total = await _db.WalletMaintenances.LongCountAsync();
            var items = await _db.WalletMaintenances
                .AsNoTracking()
                .OrderByDescending(w => w.UpdatedOn)
                .ThenBy(w => w.UserId)
                .Skip(offset)
                .Take(pageSize)
                .ToListAsync();

            return (items, total);
        }

        public async Task<(List<WalletMaintenance> Items, long Total)> GetByStatusPaginatedAsync(DebtStatus status, int page, int pageSize)
        {
            page     = Math.Max(page, 1);
            pageSize = Math.Clamp(pageSize, 1, 100);
            var offset = (page - 1) * pageSize;

            var query = _db.WalletMaintenances.Where(w => w.Status == status);
            var total = await query.LongCountAsync();
            var items = await query
                .AsNoTracking()
                .OrderByDescending(w => w.UpdatedOn)
                .ThenBy(w => w.UserId)
                .Skip(offset)
                .Take(pageSize)
                .ToListAsync();

            return (items, total);
        }
    }
}