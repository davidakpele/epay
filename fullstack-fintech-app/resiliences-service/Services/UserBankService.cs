using System.Text.Json;
using resiliences_service.Models;
using resiliences_service.interfaces;
using resiliences_service.Payloads;

namespace resiliences_service.Services
{
    public class UserBankService : IUserBankService
    {
        private readonly IUserBankRepository _repo;
        private readonly HttpClient _httpClient;
        private readonly IConfiguration _config;
        private readonly ILogger<UserBankService> _logger;

        public UserBankService(
            IUserBankRepository repo,
            IHttpClientFactory httpClientFactory,
            IConfiguration config,
            ILogger<UserBankService> logger)
        {
            _repo       = repo;
            _httpClient = httpClientFactory.CreateClient("Paystack");
            _config     = config;
            _logger     = logger;
        }

        public async Task CreateBankAsync(UserBankList bank) =>
            await _repo.CreateAsync(bank);

        public async Task<UserBankList?> FindByIdAsync(uint id) =>
            await _repo.FindByIdAsync(id);

        public async Task<UserBankList?> FindByAccountNumberAsync(string accountNumber) =>
            await _repo.FindByAccountNumberAsync(accountNumber);

        public async Task<List<UserBankList>> FindByUserIdAsync(uint userId) =>
            await _repo.FindByUserIdAsync(userId);

        public async Task DeleteByIdsAsync(List<uint> ids) =>
            await _repo.DeleteByIdsAsync(ids);

        public async Task<bool> FindByAccountNumberAndBankNameAsync(string accountNumber, string bankName) =>
            await _repo.FindByAccountNumberAndBankNameAsync(accountNumber, bankName);

        public async Task<UserBankList?> FindInternalAsync(string accountNumber, string bankCode) =>
            await _repo.FindInternalAsync(accountNumber, bankCode);

        public async Task<List<PayStackBankList>> FetchAllBanksAsync()
        {
            var response = await _httpClient.GetAsync("/bank");
            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();
            var result = JsonSerializer.Deserialize<PaystackBankResponse>(json,
                new JsonSerializerOptions { PropertyNameCaseInsensitive = true });

            if (result == null || !result.Status)
                throw new Exception(result?.Message ?? "Paystack API error");

            return result.Data;
        }

        public async Task<PaystackAccountData?> VerifyExternalAsync(string accountNumber, string bankCode)
        {
            var response = await _httpClient.GetAsync(
                $"/bank/resolve?account_number={accountNumber}&bank_code={bankCode}");

            response.EnsureSuccessStatusCode();

            var json = await response.Content.ReadAsStringAsync();
            var result = JsonSerializer.Deserialize<PaystackAccountResponse>(json,
                new JsonSerializerOptions { PropertyNameCaseInsensitive = true });

            if (result == null || !result.Status)
                throw new Exception(result?.Message ?? "Paystack API error");

            return result.Data;
        }
    }
}