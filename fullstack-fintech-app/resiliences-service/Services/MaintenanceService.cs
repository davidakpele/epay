// Services/MaintenanceService.cs
using System.Net.Http.Json;
using resiliences_service.DTOs;
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
        private readonly HttpClient                        _client;
        private readonly string                            _userServiceUrl;
        private readonly string                            _walletServiceUrl;
        private readonly string                            _revenueServiceUrl;
        private readonly string                            _notificationServiceUrl;

        public MaintenanceService(
            IWalletMaintenanceRepository     walletMaintenanceRepo,
            IMaintenanceFeeHistoryRepository feeHistoryRepo,
            IHistoryService                   historyService,
            HttpClient                        client,
            string                            userServiceUrl,
            string                            walletServiceUrl,
            string                            revenueServiceUrl,
            string                            notificationServiceUrl)
        {
            _walletMaintenanceRepo  = walletMaintenanceRepo;
            _feeHistoryRepo         = feeHistoryRepo;
            _historyService         = historyService;
            _client                 = client;
            _userServiceUrl         = userServiceUrl;
            _walletServiceUrl       = walletServiceUrl;
            _revenueServiceUrl      = revenueServiceUrl;
            _notificationServiceUrl = notificationServiceUrl;
        }

        public async Task RunAsync()
        {
            List<UserDTO> users;
            try { users = await FetchUsersAsync(); }
            catch (Exception e) { throw new Exception($"FAILED to fetch users: {e.Message}"); }

            foreach (var user in users)
            {
                WalletData walletData;
                try { walletData = await FetchWalletsAsync(user.Id); }
                catch { continue; }

                // ✅ Use IHistoryService directly instead of HTTP call
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

        // ✅ Now queries local DB via IHistoryService
        private async Task<List<History>> FetchUserHistoryAsync(long userId)
        {
            var since    = DateTime.UtcNow.AddDays(-30);
            var ulongId  = (ulong)userId;
            var history  = await _historyService.GetByUserIdAsync(ulongId);
            return history.Where(h => h.CreatedOn >= since).ToList();
        }

        // ✅ Now works directly on History model — no DTO needed
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

        private async Task<List<UserDTO>> FetchUsersAsync()
        {
            var response = await _client.GetAsync($"{_userServiceUrl}/cache/users/all");
            if (!response.IsSuccessStatusCode)
            {
                var error = await response.Content.ReadAsStringAsync();
                throw new Exception($"User service returned {response.StatusCode}: {error}");
            }
            var result = await response.Content.ReadFromJsonAsync<UserResponse>();
            return result!.Data;
        }

        private async Task<WalletData> FetchWalletsAsync(long userId)
        {
            var response = await _client.GetAsync($"{_walletServiceUrl}/wallet/cache/{userId}");
            if (!response.IsSuccessStatusCode)
            {
                var error = await response.Content.ReadAsStringAsync();
                throw new Exception($"Wallet service returned {response.StatusCode}: {error}");
            }
            var result = await response.Content.ReadFromJsonAsync<WalletResponse>();
            return result!.Wallet;
        }

        private async Task ChargeFeeAsync(UserDTO user, WalletData walletData, WalletBalanceDTO balance, decimal totalSpent)
        {
            const decimal feeRate    = 0.005m;
            const decimal minimumFee = 0.01m;

            var feeAmount                = totalSpent * feeRate;
            var previousBalance          = balance.Balance;
            var availableBalanceAfterFee = previousBalance - feeAmount;

            if (!Enum.TryParse<CurrencyType>(balance.CurrencyCode, out var currencyType)) return;
            if (feeAmount   <= minimumFee) return;
            if (totalSpent == 0)           return;

            var lastCharged = await _walletMaintenanceRepo.GetLastChargedDateAsync(user.Id, currencyType);
            if (lastCharged.HasValue && (DateTime.UtcNow - lastCharged.Value).TotalDays < 30) return;

            await _walletMaintenanceRepo.InitializeOrUpdateAsync(user.Id, currencyType, balance.Balance);

            if (balance.Balance >= feeAmount && balance.Balance > 0)
            {
                try
                {
                    await DeductFromWalletAsync(user.Id, walletData.Id, balance.CurrencyCode, feeAmount);
                    try
                    {
                        await RecordRevenueAsync(balance.CurrencyCode, feeAmount);
                        await _walletMaintenanceRepo.DeductFeeAsync(user.Id, currencyType, feeAmount);
                        await _feeHistoryRepo.RecordPaidAsync(user.Id, currencyType, feeAmount);
                        await SendNotificationAsync(user, "MAINTENANCE_FEE_PAID", balance.CurrencyCode,
                            totalSpent, feeAmount, previousBalance, availableBalanceAfterFee,
                            "Monthly maintenance fee charged on your transaction activities", true);
                    }
                    catch (Exception revenueError)
                    {
                        try { await ReverseWalletDeductionAsync(user.Id, walletData.Id, balance.CurrencyCode, feeAmount); }
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

        private async Task DeductFromWalletAsync(long userId, long walletId, string currencyCode, decimal feeAmount)
        {
            var body = new
            {
                userId,
                walletId,
                currencyType = currencyCode,
                amount       = feeAmount.ToString("F2"),
                description  = "Monthly maintenance fee on transaction activities",
                referenceNo  = $"MAINT-{userId}-{DateTimeOffset.UtcNow.ToUnixTimeSeconds()}"
            };

            var response = await _client.PostAsJsonAsync($"{_walletServiceUrl}/wallet/internal/debit/maintenance", body);
            if (!response.IsSuccessStatusCode)
            {
                var error = await response.Content.ReadAsStringAsync();
                throw new Exception($"Wallet service returned {response.StatusCode}: {error}");
            }
        }

        private async Task RecordRevenueAsync(string currencyCode, decimal feeAmount)
        {
            var body = new
            {
                transactionType = "MAINTENANCE_FEE",
                amount          = Math.Round(feeAmount, 2),
                currency        = currencyCode
            };

            var response = await _client.PostAsJsonAsync($"{_revenueServiceUrl}/api/revenue/transactions", body);
            if (!response.IsSuccessStatusCode)
            {
                var error = await response.Content.ReadAsStringAsync();
                throw new Exception($"Revenue service returned {response.StatusCode}: {error}");
            }
        }

        private async Task ReverseWalletDeductionAsync(long userId, long walletId, string currencyCode, decimal feeAmount)
        {
            var body = new
            {
                userId,
                walletId,
                currencyType = currencyCode,
                amount       = feeAmount.ToString("F2"),
                description  = "Reversal: Maintenance fee deduction failed",
                referenceNo  = $"REV-MAINT-{userId}-{DateTimeOffset.UtcNow.ToUnixTimeSeconds()}"
            };

            var response = await _client.PostAsJsonAsync($"{_walletServiceUrl}/wallet/internal/credit/maintenance", body);
            if (!response.IsSuccessStatusCode)
            {
                var error = await response.Content.ReadAsStringAsync();
                throw new Exception($"Wallet reversal returned {response.StatusCode}: {error}");
            }
        }

        private async Task SendNotificationAsync(
            UserDTO user, string actionType, string currencyCode,
            decimal totalAmountSpent, decimal feeAmount,
            decimal previousBalance, decimal availableBalance,
            string reason, bool success)
        {
            var (firstName, lastName) = ExtractUserNames(user);
            var body = new
            {
                userId           = user.Id,
                userEmail        = user.Email,
                userFirstName    = firstName,
                userLastName     = lastName,
                actionType,
                currency         = currencyCode,
                totalAmountSpent = Math.Round(totalAmountSpent, 2),
                feeAmount        = Math.Round(feeAmount, 2),
                previousBalance  = Math.Round(previousBalance, 2),
                availableBalance = Math.Round(availableBalance, 2),
                reason,
                success,
                timestamp        = DateTime.UtcNow.ToString("o")
            };

            try { await _client.PostAsJsonAsync($"{_notificationServiceUrl}/notifications/maintenance-fee", body); }
            catch { /* notification failure is non-critical */ }
        }

        private (string firstName, string lastName) ExtractUserNames(UserDTO user)
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