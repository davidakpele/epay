// Clients/RevenueServiceClient.cs
using System.Net.Http.Json;

namespace resiliences_service.Clients
{
    public class RevenueServiceClient
    {
        private readonly HttpClient          _httpClient;
        private readonly IHttpContextAccessor _httpContextAccessor;

        public RevenueServiceClient(HttpClient httpClient, IHttpContextAccessor httpContextAccessor)
        {
            _httpClient           = httpClient;
            _httpContextAccessor  = httpContextAccessor;
        }

        private void ForwardAuthToken()
        {
            var authHeader = _httpContextAccessor.HttpContext?.Request.Headers["Authorization"].FirstOrDefault();
            if (!string.IsNullOrEmpty(authHeader))
            {
                _httpClient.DefaultRequestHeaders.Remove("Authorization");
                _httpClient.DefaultRequestHeaders.Add("Authorization", authHeader);
            }
        }

        public async Task<bool> RecordMaintenanceFeeAsync(string currencyCode, decimal amount)
        {
            ForwardAuthToken();
            var body = new
            {
                transactionType = "MAINTENANCE_FEE",
                amount          = Math.Round(amount, 2),
                currency        = currencyCode
            };
            var response = await _httpClient.PostAsJsonAsync("/revenue/transactions", body);
            return response.IsSuccessStatusCode;
        }
    }
}