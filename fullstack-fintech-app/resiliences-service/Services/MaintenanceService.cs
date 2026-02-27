// Services/MaintenanceService.cs
using resiliences_service.Clients;
using resiliences_service.Enums;
using resiliences_service.interfaces;
using resiliences_service.Models;
using resiliences_service.Resopones;

namespace resiliences_service.Services
{
    public class MaintenanceService : IMaintenanceService
    {
        private readonly IWalletMaintenanceRepository     _walletMaintenanceRepo;
        private readonly IMaintenanceFeeHistoryRepository _feeHistoryRepo;
        private readonly IHistoryService                   _historyService;
        private readonly UserServiceClient                 _userClient;
        private readonly WalletServiceClient               _walletClient;
        private readonly RevenueServiceClient              _revenueClient;
        private readonly NotificationServiceClient         _notificationClient;

        public MaintenanceService(
            IWalletMaintenanceRepository     walletMaintenanceRepo,
            IMaintenanceFeeHistoryRepository feeHistoryRepo,
            IHistoryService                   historyService,
            UserServiceClient                 userClient,
            WalletServiceClient               walletClient,
            RevenueServiceClient              revenueClient,
            NotificationServiceClient         notificationClient)
        {
            _walletMaintenanceRepo = walletMaintenanceRepo;
            _feeHistoryRepo        = feeHistoryRepo;
            _historyService        = historyService;
            _userClient            = userClient;
            _walletClient          = walletClient;
            _revenueClient         = revenueClient;
            _notificationClient    = notificationClient;
        }

        public async Task RunAsync()
        {
            List<DTOs.UserDTO> users;
            try { users = await FetchUsersAsync(); }
            catch (Exception e) { throw new Exception($"FAILED to fetch users: {e.Message}"); }

            foreach (var user in users)
            {
                WalletData walletData;
                try { walletData = await FetchWalletsAsync(user.Id); }
                catch { continue; }

                List<History> userHistory;
                try { userHistory = await FetchUserHistoryAsync(user.Id); }
                catch { continue; }

                var currencyTotals = CalculateChargeableTotals(userHistory);
                if (!currencyTotals.Any()) continue;

                foreach (var (currencyCode, totalSpent) in currencyTotals)
                {
                    var balance = walletData.Balances.FirstOrDefault(b => b.CurrencyCode == currencyCode);
                    if (balance != null)
                        await ChargeFeeAsync(user, walletData, balance, totalSpent);
                }
            }
        }

        private async Task<List<History>> FetchUserHistoryAsync(long userId)
        {
            var since   = DateTime.UtcNow.AddDays(-30);
            var ulongId = (ulong)userId;
            var history = await _historyService.GetByUserIdAsync(ulongId);
            return history.Where(h => h.CreatedOn >= since).ToList();
        }

        private Dictionary<string, decimal> CalculateChargeableTotals(List<History> history)
        {
            var chargeableTypes = new HashSet<string> { "WITHDRAW", "TRANSFER", "SWAP", "DEBITED", "EXCHANGE" };
            var totals          = new Dictionary<string, decimal>();

            foreach (var record in history)
            {
                var type = record.Type?.ToString();
                if (type == null || !chargeableTypes.Contains(type)) continue;
                totals.TryGetValue(record.CurrencyType, out var current);
                totals[record.CurrencyType] = current + (decimal)record.Amount;
            }
            return totals;
        }

        private async Task<List<DTOs.UserDTO>> FetchUsersAsync()
        {
            var users = await _userClient.GetAllUsersAsync();
            return users ?? throw new Exception("User service returned empty response");
        }

        private async Task<WalletData> FetchWalletsAsync(long userId)
        {
            var wallet = await _walletClient.GetWalletByUserIdAsync(userId);
            return wallet ?? throw new Exception($"Wallet not found for user {userId}");
        }

        private async Task ChargeFeeAsync(DTOs.UserDTO user, WalletData walletData, DTOs.WalletBalanceDTO balance, decimal totalSpent)
        {
            const decimal feeRate    = 0.005m;
            const decimal minimumFee = 0.01m;

            var feeAmount                = totalSpent * feeRate;
            var previousBalance          = balance.Balance;
            var availableBalanceAfterFee = previousBalance - feeAmount;

            if (!Enum.TryParse<CurrencyType>(balance.CurrencyCode, out var currencyType)) return;
            if (feeAmount  <= minimumFee) return;
            if (totalSpent == 0)          return;

            var lastCharged = await _walletMaintenanceRepo.GetLastChargedDateAsync(user.Id, currencyType);
            if (lastCharged.HasValue && (DateTime.UtcNow - lastCharged.Value).TotalDays < 30) return;

            await _walletMaintenanceRepo.InitializeOrUpdateAsync(user.Id, currencyType, balance.Balance);

            if (balance.Balance >= feeAmount && balance.Balance > 0)
            {
                try
                {
                    var deducted = await _walletClient.DebitMaintenanceFeeAsync(
                        user.Id, walletData.Id, balance.CurrencyCode, feeAmount);

                    if (!deducted)
                        throw new Exception("Wallet debit returned failure");

                    try
                    {
                        var recorded = await _revenueClient.RecordMaintenanceFeeAsync(balance.CurrencyCode, feeAmount);

                        if (!recorded)
                            throw new Exception("Revenue service returned failure");

                        await _walletMaintenanceRepo.DeductFeeAsync(user.Id, currencyType, feeAmount);
                        await _feeHistoryRepo.RecordPaidAsync(user.Id, currencyType, feeAmount);
                        await SendNotificationAsync(user, "MAINTENANCE_FEE_PAID", balance.CurrencyCode,
                            totalSpent, feeAmount, previousBalance, availableBalanceAfterFee,
                            "Monthly maintenance fee charged on your transaction activities", true);
                    }
                    catch (Exception revenueError)
                    {
                        try { await _walletClient.CreditMaintenanceReversalAsync(user.Id, walletData.Id, balance.CurrencyCode, feeAmount); }
                        catch { /* critical reversal failure — swallow to continue */ }

                        await _walletMaintenanceRepo.MarkOverdueAsync(user.Id, currencyType);
                        await _feeHistoryRepo.RecordOverdueAsync(user.Id, currencyType, feeAmount,
                            $"Revenue recording failed: {revenueError.Message}");
                    }
                }
                catch (Exception walletError)
                {
                    await _walletMaintenanceRepo.MarkOverdueAsync(user.Id, currencyType);
                    await _feeHistoryRepo.RecordOverdueAsync(user.Id, currencyType, feeAmount,
                        $"Wallet deduction failed: {walletError.Message}");
                }
            }
            else
            {
                await _walletMaintenanceRepo.MarkOverdueAsync(user.Id, currencyType);
                await _feeHistoryRepo.RecordOverdueAsync(user.Id, currencyType, feeAmount, "Insufficient balance");
            }
        }

        private async Task SendNotificationAsync(
            DTOs.UserDTO user, string actionType, string currencyCode,
            decimal totalAmountSpent, decimal feeAmount,
            decimal previousBalance, decimal availableBalance,
            string reason, bool success)
        {
            var (firstName, lastName) = ExtractUserNames(user);
            await _notificationClient.SendMaintenanceFeeNotificationAsync(
                user, actionType, currencyCode,
                totalAmountSpent, feeAmount,
                previousBalance, availableBalance,
                reason, success, firstName, lastName);
        }

        private (string firstName, string lastName) ExtractUserNames(DTOs.UserDTO user)
        {
            var record    = user.Records?.FirstOrDefault();
            var firstName = record?.FirstName ?? "User";
            var lastName  = record?.LastName  ?? user.Id.ToString();
            return (firstName, lastName);
        }

        public async Task InitializeWalletAsync(long userId, CurrencyType currencyType, decimal balance) =>
            await _walletMaintenanceRepo.InitializeOrUpdateAsync(userId, currencyType, balance);

        public async Task PayFeeAsync(long userId, CurrencyType currencyType, decimal feeAmount)
        {
            await _walletMaintenanceRepo.DeductFeeAsync(userId, currencyType, feeAmount);
            await _feeHistoryRepo.RecordPaidAsync(userId, currencyType, feeAmount);
        }

        public async Task MarkOverdueAsync(long userId, CurrencyType currencyType, decimal feeAmount, string reason)
        {
            await _walletMaintenanceRepo.MarkOverdueAsync(userId, currencyType);
            await _feeHistoryRepo.RecordOverdueAsync(userId, currencyType, feeAmount, reason);
        }

        public async Task<DateTime?> GetLastChargedDateAsync(long userId, CurrencyType currencyType) =>
            await _walletMaintenanceRepo.GetLastChargedDateAsync(userId, currencyType);

        public async Task<(List<WalletMaintenance> Items, long Total)> GetAllWalletMaintenanceAsync(int page, int pageSize) =>
            await _walletMaintenanceRepo.GetAllPaginatedAsync(page, pageSize);

        public async Task<(List<WalletMaintenance> Items, long Total)> GetWalletMaintenanceByStatusAsync(DebtStatus status, int page, int pageSize) =>
            await _walletMaintenanceRepo.GetByStatusPaginatedAsync(status, page, pageSize);
    }
}