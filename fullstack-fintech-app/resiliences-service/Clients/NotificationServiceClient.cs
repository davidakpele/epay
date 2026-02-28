// Clients/NotificationServiceClient.cs
using System.Net.Http.Json;
using resiliences_service.DTOs;

namespace resiliences_service.Clients
{
    public class NotificationServiceClient
    {
        private readonly HttpClient          _httpClient;
        private readonly IHttpContextAccessor _httpContextAccessor;

        public NotificationServiceClient(HttpClient httpClient, IHttpContextAccessor httpContextAccessor)
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

        public async Task SendMaintenanceFeeNotificationAsync(
            UserDTO user,
            string  actionType,
            string  currencyCode,
            decimal totalAmountSpent,
            decimal feeAmount,
            decimal previousBalance,
            decimal availableBalance,
            string  reason,
            bool    success,
            string  firstName,
            string  lastName)
        {
            ForwardAuthToken();
            var body = new
            {
                userId           = user.Id,
                userEmail        = user.Email,
                userFirstName    = firstName,
                userLastName     = lastName,
                actionType,
                currency         = currencyCode,
                totalAmountSpent = Math.Round(totalAmountSpent, 2),
                feeAmount        = Math.Round(feeAmount, 2),
                previousBalance  = Math.Round(previousBalance, 2),
                availableBalance = Math.Round(availableBalance, 2),
                reason,
                success,
                timestamp        = DateTime.UtcNow.ToString("o")
            };

            try { await _httpClient.PostAsJsonAsync("/notifications/maintenance-fee", body); }
            catch { /* non-critical */ }
        }
    }
}