using resiliences_service.Models;

namespace resiliences_service.Payloads
{
    public class InitializeWalletRequest
    {
        public long        UserId       { get; set; }
        public CurrencyType CurrencyType { get; set; }
        public decimal     Balance      { get; set; }
    }
}