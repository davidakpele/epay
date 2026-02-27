using resiliences_service.Models;

namespace resiliences_service.Payloads
{
    public class MarkOverdueRequest
    {
        public long        UserId       { get; set; }
        public CurrencyType CurrencyType { get; set; }
        public decimal     FeeAmount    { get; set; }
        public string      Reason       { get; set; } = string.Empty;
    }
}