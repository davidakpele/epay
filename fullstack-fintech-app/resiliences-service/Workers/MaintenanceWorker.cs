using resiliences_service.interfaces;

namespace resiliences_service.Workers
{
    public class MaintenanceWorker : BackgroundService
    {
        private readonly IServiceScopeFactory       _scopeFactory;
        private readonly ILogger<MaintenanceWorker> _logger;

        public MaintenanceWorker(IServiceScopeFactory scopeFactory, ILogger<MaintenanceWorker> logger)
        {
            _scopeFactory = scopeFactory;
            _logger       = logger;
        }

        private static TimeSpan GetDelayUntilEndOfMonth()
        {
            var now          = DateTime.UtcNow;
            var lastDayOfMonth = new DateTime(now.Year, now.Month, DateTime.DaysInMonth(now.Year, now.Month), 0, 0, 0, DateTimeKind.Utc);

            // If we're already past (or on) midnight of the last day, target next month's end
            if (now >= lastDayOfMonth)
            {
                var nextMonth    = now.AddMonths(1);
                lastDayOfMonth   = new DateTime(nextMonth.Year, nextMonth.Month, DateTime.DaysInMonth(nextMonth.Year, nextMonth.Month), 0, 0, 0, DateTimeKind.Utc);
            }

            return lastDayOfMonth - now;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("MaintenanceWorker started. Runs at midnight UTC on the last day of each month.");

            while (!stoppingToken.IsCancellationRequested)
            {
                var delay = GetDelayUntilEndOfMonth();
                _logger.LogInformation("Next maintenance job scheduled in {Hours:F1} hours (at {RunAt:yyyy-MM-dd HH:mm:ss} UTC)",
                    delay.TotalHours,
                    DateTime.UtcNow.Add(delay));

                try
                {
                    await Task.Delay(delay, stoppingToken);
                }
                catch (TaskCanceledException)
                {
                    break;
                }

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
            }

            _logger.LogInformation("MaintenanceWorker stopped");
        }
    }
}