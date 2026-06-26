

namespace resiliences_service.Models
{
    public class AnalyticsDataPoint
    {
        public string Label  { get; set; } = default!;
        public long   Count  { get; set; }
        public decimal Amount { get; set; } 
    }
}