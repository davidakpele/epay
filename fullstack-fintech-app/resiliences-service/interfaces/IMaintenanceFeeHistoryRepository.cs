using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using resiliences_service.Models;

namespace resiliences_service.interfaces
{
     public interface IMaintenanceFeeHistoryRepository
    {
        Task RecordPaidAsync(long userId, CurrencyType currencyType, decimal feeAmount);
        Task RecordOverdueAsync(long userId, CurrencyType currencyType, decimal feeAmount, string reason);
        Task RecordPendingAsync(long id, CurrencyType currencyType, decimal feeAmount, string v);
    }
}