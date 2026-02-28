// Clients/WalletServiceClient.cs
using System.Net.Http.Json;
using resiliences_service.DTOs;
using resiliences_service.Resopones;

namespace resiliences_service.Clients
{
    public class WalletServiceClient
    {
        private readonly HttpClient          _httpClient;
        private readonly IHttpContextAccessor _httpContextAccessor;

        public WalletServiceClient(HttpClient httpClient, IHttpContextAccessor httpContextAccessor)
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

        public async Task<WalletData?> GetWalletByUserIdAsync(long userId)
        {
            ForwardAuthToken();
            var response = await _httpClient.GetAsync($"/wallet/cache/{userId}");
            if (!response.IsSuccessStatusCode) return null;
            var result = await response.Content.ReadFromJsonAsync<WalletResponse>();
            return result?.Wallet;
        }

        public async Task<bool> DebitMaintenanceFeeAsync(long userId, long walletId, string currencyCode, decimal amount)
        {
            ForwardAuthToken();
            var body = new
            {
                userId,
                walletId,
                currencyType = currencyCode,
                amount       = amount.ToString("F2"),
                description  = "Monthly maintenance fee on transaction activities",
                referenceNo  = $"MAINT-{userId}-{DateTimeOffset.UtcNow.ToUnixTimeSeconds()}"
            };
            var response = await _httpClient.PostAsJsonAsync("/wallet/internal/debit/maintenance", body);
            return response.IsSuccessStatusCode;
        }

        public async Task<bool> CreditMaintenanceReversalAsync(long userId, long walletId, string currencyCode, decimal amount)
        {
            ForwardAuthToken();
            var body = new
            {
                userId,
                walletId,
                currencyType = currencyCode,
                amount       = amount.ToString("F2"),
                description  = "Reversal: Maintenance fee deduction failed",
                referenceNo  = $"REV-MAINT-{userId}-{DateTimeOffset.UtcNow.ToUnixTimeSeconds()}"
            };
            var response = await _httpClient.PostAsJsonAsync("/wallet/internal/credit/maintenance", body);
            return response.IsSuccessStatusCode;
        }
    }
}