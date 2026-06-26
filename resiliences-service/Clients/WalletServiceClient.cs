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

        /// <summary>Debit wallet when user locks funds into an investment.</summary>
        public async Task<bool> DebitForInvestmentAsync(long userId, long walletId, string currencyCode, decimal amount, string referenceNo)
        {
            ForwardAuthToken();
            var body = new
            {
                userId,
                walletId,
                currencyType = currencyCode,
                amount       = amount.ToString("F2"),
                description  = "Investment principal locked",
                referenceNo
            };
            var response = await _httpClient.PostAsJsonAsync("/wallet/internal/debit/investment", body);
            return response.IsSuccessStatusCode;
        }

        /// <summary>Credit wallet when a matured investment pays out (principal + profit).</summary>
        public async Task<bool> CreditInvestmentPayoutAsync(long userId, long walletId, string currencyCode, decimal amount, string referenceNo)
        {
            ForwardAuthToken();
            var body = new
            {
                userId,
                walletId,
                currencyType = currencyCode,
                amount       = amount.ToString("F2"),
                description  = "Investment maturity payout (principal + profit)",
                referenceNo
            };
            var response = await _httpClient.PostAsJsonAsync("/wallet/internal/credit/investment", body);
            return response.IsSuccessStatusCode;
        }

        /// <summary>Debit wallet when user deposits into a target savings goal.</summary>
        public async Task<bool> DebitForSavingsAsync(long userId, long walletId, string currencyCode, decimal amount, string referenceNo)
        {
            ForwardAuthToken();
            var body = new
            {
                userId,
                walletId,
                currencyType = currencyCode,
                amount       = amount.ToString("F2"),
                description  = "Target savings deposit",
                referenceNo
            };
            var response = await _httpClient.PostAsJsonAsync("/wallet/internal/debit/savings", body);
            return response.IsSuccessStatusCode;
        }

        /// <summary>Credit wallet when user withdraws from a target savings goal.</summary>
        public async Task<bool> CreditSavingsWithdrawalAsync(long userId, long walletId, string currencyCode, decimal amount, string referenceNo)
        {
            ForwardAuthToken();
            var body = new
            {
                userId,
                walletId,
                currencyType = currencyCode,
                amount       = amount.ToString("F2"),
                description  = "Target savings withdrawal",
                referenceNo
            };
            var response = await _httpClient.PostAsJsonAsync("/wallet/internal/credit/savings", body);
            return response.IsSuccessStatusCode;
        }
    }
}