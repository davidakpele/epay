using resiliences_service.DTOs;
using resiliences_service.Resopones;

namespace resiliences_service.Clients
{
    public class UserServiceClient
    {
        private readonly HttpClient _httpClient;
        private readonly IHttpContextAccessor _httpContextAccessor;

        public UserServiceClient(HttpClient httpClient, IHttpContextAccessor httpContextAccessor)
        {
            _httpClient          = httpClient;
            _httpContextAccessor = httpContextAccessor;
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

        public async Task<UserDTO?> GetUserByIdAsync(long userId)
        {
            ForwardAuthToken();

            var response = await _httpClient.GetAsync($"/user/{userId}");

            if (!response.IsSuccessStatusCode)
                return null;

            return await response.Content.ReadFromJsonAsync<UserDTO>();
        }

        public async Task<UserDTO?> GetUserByUsernameAsync(string username)
        {
            ForwardAuthToken();

            var response = await _httpClient.GetAsync($"/user/username/{username}");

            if (!response.IsSuccessStatusCode)
                return null;

            return await response.Content.ReadFromJsonAsync<UserDTO>();
        }

        public async Task<List<UserDTO>?> GetAllUsersAsync()
        {
            ForwardAuthToken();
            var response = await _httpClient.GetAsync("/cache/users/all");
            if (!response.IsSuccessStatusCode) return null;
            var result = await response.Content.ReadFromJsonAsync<UserResponse>();
            return result?.Data;
        }
    }
}