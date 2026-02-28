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
        private readonly ILogger<MaintenanceService>       _logger;

        public MaintenanceService(
            IWalletMaintenanceRepository     walletMaintenanceRepo,
            IMaintenanceFeeHistoryRepository feeHistoryRepo,
            IHistoryService                   historyService,
            UserServiceClient                 userClient,
            WalletServiceClient               walletClient,
            RevenueServiceClient              revenueClient,
            NotificationServiceClient         notificationClient,
            ILogger<MaintenanceService>       logger)
        {
            _walletMaintenanceRepo = walletMaintenanceRepo;
            _feeHistoryRepo        = feeHistoryRepo;
            _historyService        = historyService;
            _userClient            = userClient;
            _walletClient          = walletClient;
            _revenueClient         = revenueClient;
            _notificationClient    = notificationClient;
            _logger                = logger;
        }

        public async Task RunAsync()
        {
            List<DTOs.UserDTO> users;
            try { users = await FetchUsersAsync(); }
            catch (Exception e) { throw new Exception($"FAILED to fetch users: {e.Message}"); }

            _logger.LogInformation("Maintenance run started — {UserCount} users fetched", users.Count);

            foreach (var user in users)
            {
                WalletData walletData;
                try { walletData = await FetchWalletsAsync(user.Id); }
                catch (Exception e)
                {
                    _logger.LogWarning("Skipping user {UserId} — wallet fetch failed: {Reason}", user.Id, e.Message);
                    continue;
                }

                List<History> userHistory;
                try { userHistory = await FetchUserHistoryAsync(user.Id); }
                catch (Exception e)
                {
                    _logger.LogWarning("Skipping user {UserId} — history fetch failed: {Reason}", user.Id, e.Message);
                    continue;
                }

                var currencyTotals = CalculateChargeableTotals(userHistory);
                if (!currencyTotals.Any())
                {
                    _logger.LogInformation("Skipping user {UserId} — no chargeable transactions in the last 30 days", user.Id);
                    continue;
                }

                _logger.LogInformation("User {UserId} has chargeable totals: {Totals}",
                    user.Id,
                    string.Join(", ", currencyTotals.Select(t => $"{t.Key}={t.Value:F2}")));

                foreach (var (currencyCode, totalSpent) in currencyTotals)
                {
                    var balance = walletData.Balances.FirstOrDefault(b => b.CurrencyCode == currencyCode);
                    if (balance == null)
                    {
                        _logger.LogWarning("User {UserId} — no {Currency} wallet balance found, skipping", user.Id, currencyCode);
                        continue;
                    }

                    await ChargeFeeAsync(user, walletData, balance, totalSpent);
                }
            }

            _logger.LogInformation("Maintenance run completed");
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

            if (!Enum.TryParse<CurrencyType>(balance.CurrencyCode, out var currencyType))
            {
                _logger.LogWarning("User {UserId} — unrecognised currency code '{Code}', skipping", user.Id, balance.CurrencyCode);
                return;
            }

            if (totalSpent == 0) return;

            var feeAmount                = totalSpent * feeRate;
            var previousBalance          = balance.Balance;
            var availableBalanceAfterFee = previousBalance - feeAmount;

            if (feeAmount < minimumFee)
            {
                _logger.LogInformation("User {UserId} {Currency} — fee {Fee:F2} is below minimum {Min:F2}, skipping",
                    user.Id, balance.CurrencyCode, feeAmount, minimumFee);
                return;
            }

            var currentStatus = await _walletMaintenanceRepo.GetStatusAsync(user.Id, currencyType);
            if (currentStatus == DebtStatus.PENDING)
            {
                _logger.LogInformation("User {UserId} {Currency} — wallet is PENDING, skipping until resolved",
                    user.Id, balance.CurrencyCode);
                return;
            }

            var lastCharged = await _walletMaintenanceRepo.GetLastChargedDateAsync(user.Id, currencyType);
            if (lastCharged.HasValue && (DateTime.UtcNow - lastCharged.Value).TotalDays < 30)
            {
                _logger.LogInformation("User {UserId} {Currency} — already charged on {LastCharged:yyyy-MM-dd}, skipping",
                    user.Id, balance.CurrencyCode, lastCharged.Value);
                return;
            }

            _logger.LogInformation("User {UserId} {Currency} — attempting fee {Fee:F2} on balance {Balance:F2}",
                user.Id, balance.CurrencyCode, feeAmount, previousBalance);

            await _walletMaintenanceRepo.InitializeOrUpdateAsync(user.Id, currencyType, balance.Balance);

            if (balance.Balance >= feeAmount && balance.Balance > 0)
            {
                await ProcessChargeAsync(user, walletData, balance, currencyType, feeAmount, totalSpent, previousBalance, availableBalanceAfterFee);
            }
            else
            {
                _logger.LogWarning("User {UserId} {Currency} — insufficient balance ({Balance:F2}) to cover fee ({Fee:F2})",
                    user.Id, balance.CurrencyCode, previousBalance, feeAmount);
                await HandleInsufficientBalanceAsync(user, balance, currencyType, feeAmount, totalSpent, previousBalance, currentStatus);
            }
        }

        private async Task ProcessChargeAsync(
            DTOs.UserDTO          user,
            WalletData            walletData,
            DTOs.WalletBalanceDTO balance,
            CurrencyType          currencyType,
            decimal               feeAmount,
            decimal               totalSpent,
            decimal               previousBalance,
            decimal               availableBalanceAfterFee)
        {
            bool deducted;

            try
            {
                deducted = await _walletClient.DebitMaintenanceFeeAsync(
                    user.Id, walletData.Id, balance.CurrencyCode, feeAmount);
            }
            catch (Exception walletError)
            {
                _logger.LogError("User {UserId} {Currency} — wallet debit threw exception: {Error}",
                    user.Id, balance.CurrencyCode, walletError.Message);
                await _walletMaintenanceRepo.MarkOverdueAsync(user.Id, currencyType);
                await _feeHistoryRepo.RecordOverdueAsync(user.Id, currencyType, feeAmount, $"Wallet deduction failed: {walletError.Message}");
                await SendNotificationAsync(user, "MAINTENANCE_FEE_OVERDUE", balance.CurrencyCode,
                    totalSpent, feeAmount, previousBalance, previousBalance,
                    "Maintenance fee deduction failed — marked overdue", false);
                return;
            }

            if (!deducted)
            {
                _logger.LogError("User {UserId} {Currency} — wallet debit returned failure response",
                    user.Id, balance.CurrencyCode);
                await _walletMaintenanceRepo.MarkOverdueAsync(user.Id, currencyType);
                await _feeHistoryRepo.RecordOverdueAsync(user.Id, currencyType, feeAmount, "Wallet debit returned failure");
                await SendNotificationAsync(user, "MAINTENANCE_FEE_OVERDUE", balance.CurrencyCode,
                    totalSpent, feeAmount, previousBalance, previousBalance,
                    "Maintenance fee deduction failed — marked overdue", false);
                return;
            }

            _logger.LogInformation("User {UserId} {Currency} — wallet debit successful, recording revenue",
                user.Id, balance.CurrencyCode);

            await RecordRevenueAsync(user, walletData, balance, currencyType, feeAmount, totalSpent, previousBalance, availableBalanceAfterFee);
        }

        private async Task RecordRevenueAsync(
            DTOs.UserDTO          user,
            WalletData            walletData,
            DTOs.WalletBalanceDTO balance,
            CurrencyType          currencyType,
            decimal               feeAmount,
            decimal               totalSpent,
            decimal               previousBalance,
            decimal               availableBalanceAfterFee)
        {
            try
            {
                var recorded = await _revenueClient.RecordMaintenanceFeeAsync(balance.CurrencyCode, feeAmount);

                if (!recorded)
                    throw new Exception("Revenue service returned failure");

                await _walletMaintenanceRepo.DeductFeeAsync(user.Id, currencyType, feeAmount);
                await _feeHistoryRepo.RecordPaidAsync(user.Id, currencyType, feeAmount);

                _logger.LogInformation("User {UserId} {Currency} — fee {Fee:F2} charged and recorded successfully",
                    user.Id, balance.CurrencyCode, feeAmount);

                await SendNotificationAsync(user, "MAINTENANCE_FEE_PAID", balance.CurrencyCode,
                    totalSpent, feeAmount, previousBalance, availableBalanceAfterFee,
                    "Monthly maintenance fee charged on your transaction activities", true);
            }
            catch (Exception revenueError)
            {
                _logger.LogError("User {UserId} {Currency} — revenue recording failed: {Error}. Attempting reversal",
                    user.Id, balance.CurrencyCode, revenueError.Message);

                try
                {
                    await _walletClient.CreditMaintenanceReversalAsync(user.Id, walletData.Id, balance.CurrencyCode, feeAmount);
                    _logger.LogInformation("User {UserId} {Currency} — reversal credited successfully", user.Id, balance.CurrencyCode);
                }
                catch (Exception reversalError)
                {
                    _logger.LogCritical("User {UserId} {Currency} — reversal FAILED: {Error}. Manual intervention required",
                        user.Id, balance.CurrencyCode, reversalError.Message);
                }

                await _walletMaintenanceRepo.MarkOverdueAsync(user.Id, currencyType);
                await _feeHistoryRepo.RecordOverdueAsync(user.Id, currencyType, feeAmount, $"Revenue recording failed: {revenueError.Message}");
                await SendNotificationAsync(user, "MAINTENANCE_FEE_OVERDUE", balance.CurrencyCode,
                    totalSpent, feeAmount, previousBalance, previousBalance,
                    "Maintenance fee could not be completed — marked overdue", false);
            }
        }

        private async Task HandleInsufficientBalanceAsync(
            DTOs.UserDTO          user,
            DTOs.WalletBalanceDTO balance,
            CurrencyType          currencyType,
            decimal               feeAmount,
            decimal               totalSpent,
            decimal               previousBalance,
            DebtStatus?           currentStatus)
        {
            if (currentStatus == DebtStatus.OVERDUE)
            {
                _logger.LogWarning("User {UserId} {Currency} — still overdue, balance insufficient ({Balance:F2})",
                    user.Id, balance.CurrencyCode, previousBalance);
                await _feeHistoryRepo.RecordOverdueAsync(user.Id, currencyType, feeAmount, "Insufficient balance — fee remains overdue");
                await SendNotificationAsync(user, "MAINTENANCE_FEE_OVERDUE", balance.CurrencyCode,
                    totalSpent, feeAmount, previousBalance, previousBalance,
                    "Your maintenance fee is still overdue due to insufficient balance", false);
            }
            else
            {
                _logger.LogWarning("User {UserId} {Currency} — marked PENDING, fee {Fee:F2} owed",
                    user.Id, balance.CurrencyCode, feeAmount);
                await _walletMaintenanceRepo.MarkPendingAsync(user.Id, currencyType);
                await _feeHistoryRepo.RecordPendingAsync(user.Id, currencyType, feeAmount, "Insufficient balance — fee pending");
                await SendNotificationAsync(user, "MAINTENANCE_FEE_PENDING", balance.CurrencyCode,
                    totalSpent, feeAmount, previousBalance, previousBalance,
                    "You have a pending maintenance fee due to insufficient balance. Please top up your wallet", false);
            }
        }

        private async Task SendNotificationAsync(
            DTOs.UserDTO user,
            string       actionType,
            string       currencyCode,
            decimal      totalAmountSpent,
            decimal      feeAmount,
            decimal      previousBalance,
            decimal      availableBalance,
            string       reason,
            bool         success)
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