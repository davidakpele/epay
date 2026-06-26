using resiliences_service.Clients;
using resiliences_service.Enums;
using resiliences_service.interfaces;
using resiliences_service.Models;
using resiliences_service.Payloads;

namespace resiliences_service.Services
{
    public class TargetSavingsService : ITargetSavingsService
    {
        private readonly ITargetSavingsRepository       _repo;
        private readonly WalletServiceClient            _walletClient;
        private readonly IHistoryService                _historyService;
        private readonly ILogger<TargetSavingsService>  _logger;

        public TargetSavingsService(
            ITargetSavingsRepository      repo,
            WalletServiceClient           walletClient,
            IHistoryService               historyService,
            ILogger<TargetSavingsService> logger)
        {
            _repo           = repo;
            _walletClient   = walletClient;
            _historyService = historyService;
            _logger         = logger;
        }

        public async Task<TargetSavings> CreateGoalAsync(CreateTargetSavingsRequest req)
        {
            var savings = new TargetSavings
            {
                UserId       = req.UserId,
                WalletId     = req.WalletId,
                CurrencyCode = req.CurrencyCode,
                GoalName     = req.GoalName,
                Description  = req.Description,
                TargetAmount = req.TargetAmount,
                SavedAmount  = 0,
                TargetDate   = req.TargetDate,
                Status       = TargetSavingsStatus.ACTIVE,
                GoalIcon     = req.GoalIcon
            };

            return await _repo.CreateAsync(savings);
        }

        public async Task<TargetSavings> TopUpAsync(long savingsId, TopUpTargetSavingsRequest req)
        {
            var savings = await _repo.FindByIdAndUserIdAsync(savingsId, req.UserId)
                ?? throw new KeyNotFoundException($"Savings goal {savingsId} not found.");

            if (savings.Status != TargetSavingsStatus.ACTIVE)
                throw new InvalidOperationException($"Cannot top up a savings goal with status {savings.Status}.");

            var refId = $"SAV-TOPUP-{req.UserId}-{DateTimeOffset.UtcNow.ToUnixTimeSeconds()}";

            // Debit the user's main wallet
            bool debited = await _walletClient.DebitForSavingsAsync(
                req.UserId, req.WalletId, savings.CurrencyCode, req.Amount, refId);

            if (!debited)
                throw new InvalidOperationException("Insufficient wallet balance or wallet service unavailable.");

            savings.SavedAmount += req.Amount;

            // Auto-complete when target is reached
            if (savings.SavedAmount >= savings.TargetAmount)
            {
                savings.Status      = TargetSavingsStatus.COMPLETED;
                savings.CompletedAt = DateTime.UtcNow;
            }

            var updated = await _repo.UpdateAsync(savings);

            // Write history
            try
            {
                await _historyService.CreateDepositAsync(new History
                {
                    UserId        = (ulong)req.UserId,
                    WalletId      = (ulong)req.WalletId,
                    CurrencyType  = savings.CurrencyCode,
                    Type          = TransactionType.DEBITED,
                    GrossAmount   = req.Amount,
                    NetAmount     = req.Amount,
                    Description   = $"Savings top-up — goal: \"{savings.GoalName}\" (ref: {refId})",
                    Status        = "SUCCESS",
                    ReferenceId   = refId,
                    TransactionId = $"TXN_{Guid.NewGuid():N}"[..20].ToUpper(),
                    SessionId     = $"SESS_{Guid.NewGuid():N}"[..20].ToUpper(),
                    Timestamp     = DateTime.UtcNow
                });
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "[TargetSavingsService] History record failed for savings top-up {Id}", savingsId);
            }

            return updated;
        }

        public async Task<TargetSavings> WithdrawAsync(long savingsId, WithdrawTargetSavingsRequest req)
        {
            var savings = await _repo.FindByIdAndUserIdAsync(savingsId, req.UserId)
                ?? throw new KeyNotFoundException($"Savings goal {savingsId} not found.");

            if (savings.SavedAmount <= 0)
                throw new InvalidOperationException("No funds available to withdraw.");

            if (savings.Status == TargetSavingsStatus.WITHDRAWN)
                throw new InvalidOperationException("Funds have already been withdrawn from this goal.");

            var amountToReturn = savings.SavedAmount;
            var refId = $"SAV-WDRW-{req.UserId}-{DateTimeOffset.UtcNow.ToUnixTimeSeconds()}";

            // Credit the user's main wallet
            bool credited = await _walletClient.CreditSavingsWithdrawalAsync(
                req.UserId, req.WalletId, savings.CurrencyCode, amountToReturn, refId);

            if (!credited)
                throw new InvalidOperationException("Wallet credit failed — please try again.");

            savings.Status      = TargetSavingsStatus.WITHDRAWN;
            savings.WithdrawnAt = DateTime.UtcNow;
            savings.SavedAmount = 0;

            var updated = await _repo.UpdateAsync(savings);

            // Write history
            try
            {
                await _historyService.CreateCreditAsync(new History
                {
                    UserId        = (ulong)req.UserId,
                    WalletId      = (ulong)req.WalletId,
                    CurrencyType  = savings.CurrencyCode,
                    Type          = TransactionType.CREDITED,
                    GrossAmount   = amountToReturn,
                    NetAmount     = amountToReturn,
                    Description   = $"Savings withdrawal — goal: \"{savings.GoalName}\" (ref: {refId})",
                    Status        = "SUCCESS",
                    ReferenceId   = refId,
                    TransactionId = $"TXN_{Guid.NewGuid():N}"[..20].ToUpper(),
                    SessionId     = $"SESS_{Guid.NewGuid():N}"[..20].ToUpper(),
                    Timestamp     = DateTime.UtcNow
                });
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "[TargetSavingsService] History record failed for savings withdrawal {Id}", savingsId);
            }

            return updated;
        }

        public async Task<TargetSavings?> GetByIdAsync(long id, long userId) =>
            await _repo.FindByIdAndUserIdAsync(id, userId);

        public async Task<List<TargetSavings>> GetByUserIdAsync(long userId) =>
            await _repo.FindByUserIdAsync(userId);

        public async Task CancelAsync(long savingsId, long userId)
        {
            var savings = await _repo.FindByIdAndUserIdAsync(savingsId, userId)
                ?? throw new KeyNotFoundException($"Savings goal {savingsId} not found.");

            if (savings.Status != TargetSavingsStatus.ACTIVE)
                throw new InvalidOperationException($"Cannot cancel a goal with status {savings.Status}. Withdraw funds first if applicable.");

            if (savings.SavedAmount > 0)
                throw new InvalidOperationException("Please withdraw your funds before cancelling this goal.");

            savings.Status = TargetSavingsStatus.CANCELLED;
            await _repo.UpdateAsync(savings);
        }
    }
}
