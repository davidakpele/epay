using resiliences_service.interfaces;

namespace resiliences_service.Workers
{
    public class MaintenanceWorker : BackgroundService
    {
        private readonly IServiceScopeFactory      _scopeFactory;
        private readonly ILogger<MaintenanceWorker> _logger;
        private readonly TimeSpan                   _interval;

        public MaintenanceWorker(IServiceScopeFactory scopeFactory, ILogger<MaintenanceWorker> logger, IConfiguration config)
        {
            _scopeFactory = scopeFactory;
            _logger       = logger;
            _interval     = TimeSpan.FromDays(config.GetValue<int>("MaintenanceJob:IntervalDays", 30));
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("MaintenanceWorker started. Interval: {Days} day(s)", _interval.TotalDays);

            while (!stoppingToken.IsCancellationRequested)
            {
                _logger.LogInformation("Maintenance job triggered at {Time}", DateTimeOffset.UtcNow);

                try
                {
                    using var scope   = _scopeFactory.CreateScope();
                    var       service = scope.ServiceProvider.GetRequiredService<IMaintenanceService>();
                    await service.RunAsync();
                    _logger.LogInformation("Maintenance job completed at {Time}", DateTimeOffset.UtcNow);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Maintenance job failed at {Time}", DateTimeOffset.UtcNow);
                }

                await Task.Delay(_interval, stoppingToken);
            }

            _logger.LogInformation("MaintenanceWorker stopped");
        }
    }
}