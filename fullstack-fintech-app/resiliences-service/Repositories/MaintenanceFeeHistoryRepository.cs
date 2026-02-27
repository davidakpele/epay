using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using resiliences_service.Configs;
using resiliences_service.Enums;
using resiliences_service.interfaces;
using resiliences_service.Models;

namespace resiliences_service.Repositories
{
     public class MaintenanceFeeHistoryRepository : IMaintenanceFeeHistoryRepository
    {
        private readonly AppDbContext _db;

        public MaintenanceFeeHistoryRepository(AppDbContext db)
        {
            _db = db;
        }

        public async Task RecordPaidAsync(long userId, CurrencyType currencyType, decimal feeAmount)
        {
            _db.MaintenanceFeeHistories.Add(new MaintenanceFeeHistory
            {
                UserId       = userId,
                CurrencyType = currencyType,
                FeeAmount    = feeAmount,
                Status       = DebtStatus.PAID,
                AttemptedOn  = DateTime.UtcNow,
                PaidOn       = DateTime.UtcNow
            });

            await _db.SaveChangesAsync();
        }

        public async Task RecordOverdueAsync(long userId, CurrencyType currencyType, decimal feeAmount, string reason)
        {
            _db.MaintenanceFeeHistories.Add(new MaintenanceFeeHistory
            {
                UserId       = userId,
                CurrencyType = currencyType,
                FeeAmount    = feeAmount,
                Status       = DebtStatus.OVERDUE,
                Reason       = reason,
                AttemptedOn  = DateTime.UtcNow
            });

            await _db.SaveChangesAsync();
        }
    }
}