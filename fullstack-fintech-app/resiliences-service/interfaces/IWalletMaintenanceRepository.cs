using resiliences_service.Enums;
using resiliences_service.Models;

namespace resiliences_service.interfaces
{
    public interface IWalletMaintenanceRepository
    {
        Task InitializeOrUpdateAsync(long userId, CurrencyType currencyType, decimal balance);
        Task DeductFeeAsync(long userId, CurrencyType currencyType, decimal amount);
        Task MarkPendingAsync(long userId, CurrencyType currencyType);
        Task MarkOverdueAsync(long userId, CurrencyType currencyType);
        Task<DebtStatus?> GetStatusAsync(long userId, CurrencyType currencyType);
        Task<DateTime?> GetLastChargedDateAsync(long userId, CurrencyType currencyType);
        Task<(List<WalletMaintenance> Items, long Total)> GetAllPaginatedAsync(int page, int pageSize);
        Task<(List<WalletMaintenance> Items, long Total)> GetByStatusPaginatedAsync(DebtStatus status, int page, int pageSize);
    }
}