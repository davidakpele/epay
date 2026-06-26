using resiliences_service.Clients;
using resiliences_service.Enums;
using resiliences_service.interfaces;
using resiliences_service.Models;
using resiliences_service.Payloads;

namespace resiliences_service.Services
{
    public class InvestmentService : IInvestmentService
    {
        // ── Return rates per duration (annualized → prorated) ──────────────────
        // Weekly  → 8%  per year  → ~0.154% per week
        // Monthly → 12% per year  → 1% per month
        // Quarterly → 18% per year → 4.5% per quarter
        // Yearly  → 24% per year
        private static readonly Dictionary<InvestmentDuration, (decimal AnnualRate, int Days)> _plans = new()
        {
            { InvestmentDuration.WEEKLY,    (8m,   7)   },
            { InvestmentDuration.MONTHLY,   (12m,  30)  },
            { InvestmentDuration.QUARTERLY, (18m,  90)  },
            { InvestmentDuration.YEARLY,    (24m,  365) },
        };

        private readonly IInvestmentRepository       _repo;
        private readonly WalletServiceClient         _walletClient;
        private readonly IHistoryService             _historyService;
        private readonly ILogger<InvestmentService>  _logger;

        public InvestmentService(
            IInvestmentRepository      repo,
            WalletServiceClient        walletClient,
            IHistoryService            historyService,
            ILogger<InvestmentService> logger)
        {
            _repo           = repo;
            _walletClient   = walletClient;
            _historyService = historyService;
            _logger         = logger;
        }

        // ── helpers ────────────────────────────────────────────────────────────

        private static (decimal rate, int days) GetPlan(InvestmentDuration duration) =>
            _plans.TryGetValue(duration, out var plan)
                ? plan
                : throw new ArgumentException($"Unknown investment duration: {duration}");

        private static decimal CalculateProfit(decimal principal, decimal annualRate, int days)
        {
            // simple interest: P × r × t   (r in fraction, t in years)
            var t = days / 365m;
            return Math.Round(principal * (annualRate / 100m) * t, 4);
        }

        // ── IInvestmentService ─────────────────────────────────────────────────

        public Task<object> CalculateReturnsAsync(decimal principal, InvestmentDuration duration, string currencyCode)
        {
            var (rate, days) = GetPlan(duration);
            var profit      = CalculateProfit(principal, rate, days);
            var total       = principal + profit;

            object result = new
            {
                principal,
                duration      = duration.ToString(),
                durationDays  = days,
                annualRate    = rate,
                expectedProfit = profit,
                totalPayout   = total,
                currencyCode,
                maturityDate  = DateTime.UtcNow.AddDays(days).ToString("yyyy-MM-dd")
            };

            return Task.FromResult(result);
        }

        public async Task<Investment> CreateInvestmentAsync(CreateInvestmentRequest req)
        {
            var (rate, days) = GetPlan(req.Duration);
            var profit  = CalculateProfit(req.Principal, rate, days);
            var total   = req.Principal + profit;
            var refId   = $"INV-{req.UserId}-{DateTimeOffset.UtcNow.ToUnixTimeSeconds()}";

            // 1. Debit the user's main wallet
            bool debited = await _walletClient.DebitForInvestmentAsync(
                req.UserId, req.WalletId, req.CurrencyCode, req.Principal, refId);

            if (!debited)
                throw new InvalidOperationException("Insufficient wallet balance or wallet service unavailable.");

            // 2. Persist the investment record
            var investment = new Investment
            {
                UserId        = req.UserId,
                WalletId      = req.WalletId,
                CurrencyCode  = req.CurrencyCode,
                Principal     = req.Principal,
                ReturnRate    = rate,
                ExpectedProfit = profit,
                TotalPayout   = total,
                Duration      = req.Duration,
                DurationDays  = days,
                StartDate     = DateTime.UtcNow,
                MaturityDate  = DateTime.UtcNow.AddDays(days),
                Status        = InvestmentStatus.ACTIVE,
                ReferenceId   = refId
            };

            var created = await _repo.CreateAsync(investment);

            // 3. Write a transaction history record
            try
            {
                await _historyService.CreateDepositAsync(new History
                {
                    UserId           = (ulong)req.UserId,
                    WalletId         = (ulong)req.WalletId,
                    CurrencyType     = req.CurrencyCode,
                    Type             = TransactionType.DEBITED,
                    GrossAmount      = req.Principal,
                    NetAmount        = req.Principal,
                    Description      = $"Investment locked — {req.Duration} plan at {rate}% p.a. (ref: {refId})",
                    Status           = "SUCCESS",
                    ReferenceId      = refId,
                    TransactionId    = $"TXN_{Guid.NewGuid():N}"[..20].ToUpper(),
                    SessionId        = $"SESS_{Guid.NewGuid():N}"[..20].ToUpper(),
                    Timestamp        = DateTime.UtcNow
                });
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "[InvestmentService] History record failed for investment {Id}", created.Id);
            }

            return created;
        }

        public async Task<Investment?> GetByIdAsync(long id, long userId)
        {
            var inv = await _repo.FindByIdAsync(id);
            return (inv == null || inv.UserId != userId) ? null : inv;
        }

        public async Task<List<Investment>> GetByUserIdAsync(long userId) =>
            await _repo.FindByUserIdAsync(userId);

        /// <summary>
        /// Called periodically by the InvestmentMaturityWorker.
        /// Finds all ACTIVE investments whose maturity date has passed and credits
        /// principal + profit back to the user's wallet.
        /// </summary>
        public async Task ProcessMaturedInvestmentsAsync()
        {
            var matured = await _repo.FindMaturedUnpaidAsync();
            _logger.LogInformation("[InvestmentService] Processing {Count} matured investments", matured.Count);

            foreach (var inv in matured)
            {
                try
                {
                    var refId = $"INV-PAYOUT-{inv.UserId}-{DateTimeOffset.UtcNow.ToUnixTimeSeconds()}";

                    bool credited = await _walletClient.CreditInvestmentPayoutAsync(
                        inv.UserId, inv.WalletId, inv.CurrencyCode, inv.TotalPayout, refId);

                    if (!credited)
                    {
                        _logger.LogError("[InvestmentService] Payout failed for investment {Id}", inv.Id);
                        inv.Status = InvestmentStatus.FAILED;
                        await _repo.UpdateAsync(inv);
                        continue;
                    }

                    inv.Status    = InvestmentStatus.PAID_OUT;
                    inv.PaidOutAt = DateTime.UtcNow;
                    await _repo.UpdateAsync(inv);

                    // Write history
                    try
                    {
                        await _historyService.CreateCreditAsync(new History
                        {
                            UserId        = (ulong)inv.UserId,
                            WalletId      = (ulong)inv.WalletId,
                            CurrencyType  = inv.CurrencyCode,
                            Type          = TransactionType.CREDITED,
                            GrossAmount   = inv.TotalPayout,
                            NetAmount     = inv.TotalPayout,
                            Description   = $"Investment matured — principal {inv.Principal:F2} + profit {inv.ExpectedProfit:F2} (ref: {refId})",
                            Status        = "SUCCESS",
                            ReferenceId   = refId,
                            TransactionId = $"TXN_{Guid.NewGuid():N}"[..20].ToUpper(),
                            SessionId     = $"SESS_{Guid.NewGuid():N}"[..20].ToUpper(),
                            Timestamp     = DateTime.UtcNow
                        });
                    }
                    catch (Exception ex)
                    {
                        _logger.LogWarning(ex, "[InvestmentService] Payout history record failed for investment {Id}", inv.Id);
                    }

                    _logger.LogInformation("[InvestmentService] Payout processed for investment {Id}, user {UserId}, amount {Amount}",
                        inv.Id, inv.UserId, inv.TotalPayout);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "[InvestmentService] Unexpected error processing investment {Id}", inv.Id);
                }
            }
        }
    }
}
