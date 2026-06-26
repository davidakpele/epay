using resiliences_service.interfaces;

namespace resiliences_service.Workers
{
    /// <summary>
    /// Background service that runs every hour and processes matured investments.
    /// When an investment's MaturityDate is reached and its Status is still ACTIVE,
    /// this worker credits the user's wallet with principal + profit and marks it PAID_OUT.
    /// </summary>
    public class InvestmentMaturityWorker : BackgroundService
    {
        private static readonly TimeSpan CheckInterval = TimeSpan.FromHours(1);

        private readonly IServiceScopeFactory              _scopeFactory;
        private readonly ILogger<InvestmentMaturityWorker> _logger;

        public InvestmentMaturityWorker(IServiceScopeFactory scopeFactory, ILogger<InvestmentMaturityWorker> logger)
        {
            _scopeFactory = scopeFactory;
            _logger       = logger;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("InvestmentMaturityWorker started. Checking every {Interval}.", CheckInterval);

            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    using var scope   = _scopeFactory.CreateScope();
                    var       service = scope.ServiceProvider.GetRequiredService<IInvestmentService>();
                    await service.ProcessMaturedInvestmentsAsync();
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "InvestmentMaturityWorker encountered an error.");
                }

                try
                {
                    await Task.Delay(CheckInterval, stoppingToken);
                }
                catch (TaskCanceledException)
                {
                    break;
                }
            }

            _logger.LogInformation("InvestmentMaturityWorker stopped.");
        }
    }
}
