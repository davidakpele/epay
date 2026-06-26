using System.IdentityModel.Tokens.Jwt;
using System.Text.Json;
using Microsoft.IdentityModel.Tokens;
using resiliences_service.Clients;
using resiliences_service.DTOs;
using resiliences_service.Resopones;

namespace resiliences_service.Middleware
{
    public class SecurityMiddleware
    {
        private readonly RequestDelegate _next;
        private readonly IConfiguration _configuration;

        public SecurityMiddleware(RequestDelegate next, IConfiguration configuration)
        {
            _next          = next;
            _configuration = configuration;
        }

        public async Task InvokeAsync(HttpContext context, UserServiceClient userServiceClient)
        {
            var path      = context.Request.Path.Value ?? "";
            var requestId = Guid.NewGuid().ToString("N")[..8].ToUpper();

            if (path.StartsWith("/swagger", StringComparison.OrdinalIgnoreCase))
            {
                await _next(context);
                return;
            }

            var authHeader = context.Request.Headers["Authorization"].FirstOrDefault();
            if (string.IsNullOrEmpty(authHeader) || !authHeader.StartsWith("Bearer "))
            {
                await WriteErrorAsync(context, 401, ApiResponse.Unauthorized("No token provided"));
                return;
            }

            var token        = authHeader["Bearer ".Length..].Trim();
            var tokenHandler = new JwtSecurityTokenHandler();

            if (!tokenHandler.CanReadToken(token))
            {
                await WriteErrorAsync(context, 401, ApiResponse.TokenInvalid());
                return;
            }

            var jwtSettings = _configuration.GetSection("JwtSettings");
            var secretKey   = jwtSettings["SecretKey"];
            var issuer      = jwtSettings["Issuer"];
            var audience    = jwtSettings["Audience"];

            if (string.IsNullOrEmpty(secretKey) || string.IsNullOrEmpty(issuer) || string.IsNullOrEmpty(audience))
            {
                await WriteErrorAsync(context, 500, ApiResponse.ServerError());
                return;
            }

            byte[] keyBytes;
            try
            {
                keyBytes = Convert.FromBase64String(secretKey);
            }
            catch (FormatException)
            {
                await WriteErrorAsync(context, 500, ApiResponse.ServerError());
                return;
            }

            JwtSecurityToken jwtToken;
            try
            {
                var validationParams = new TokenValidationParameters
                {
                    ValidateIssuer           = true,
                    ValidateAudience         = true,
                    ValidateLifetime         = true,
                    ValidateIssuerSigningKey = true,
                    ValidIssuer              = issuer,
                    ValidAudience            = audience,
                    IssuerSigningKey         = new SymmetricSecurityKey(keyBytes),
                    ClockSkew                = TimeSpan.Zero
                };

                tokenHandler.ValidateToken(token, validationParams, out var validatedToken);

                if (validatedToken is not JwtSecurityToken parsedJwt)
                {
                    await WriteErrorAsync(context, 401, ApiResponse.TokenInvalid());
                    return;
                }

                jwtToken = parsedJwt;
            }
            catch (SecurityTokenExpiredException)
            {
                await WriteErrorAsync(context, 401, ApiResponse.TokenExpired());
                return;
            }
            catch (SecurityTokenException)
            {
                await WriteErrorAsync(context, 401, ApiResponse.TokenInvalid());
                return;
            }
            catch (Exception)
            {
                await WriteErrorAsync(context, 500, ApiResponse.ServerError());
                return;
            }

            var userId   = jwtToken.Claims.FirstOrDefault(c => c.Type == "userId")?.Value;
            var username = jwtToken.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub)?.Value;
            var roles    = jwtToken.Claims
                               .Where(c => c.Type == "role" || c.Type == "roles")
                               .Select(c => c.Value)
                               .ToHashSet();

            if (string.IsNullOrEmpty(userId))
            {
                await WriteErrorAsync(context, 401, ApiResponse.MissingClaim("userId"));
                return;
            }

            if (string.IsNullOrEmpty(username))
            {
                await WriteErrorAsync(context, 401, ApiResponse.MissingClaim("username"));
                return;
            }

            if (!long.TryParse(userId, out var parsedUserId))
            {
                await WriteErrorAsync(context, 401, ApiResponse.TokenInvalid());
                return;
            }

            // ── ADMIN / SUPER_ADMIN — JWT validation is sufficient, skip user service ──
            if (roles.Contains("ADMIN") || roles.Contains("SUPER_ADMIN"))
            {
                context.Items["RequestId"] = requestId;
                await _next(context);
                return;
            }

            // ── USER — full verification against user service required ────────────────
            UserDTO? user;
            try
            {
                user = await userServiceClient.GetUserByUsernameAsync(username);
            }
            catch (HttpRequestException)
            {
                await WriteErrorAsync(context, 503, ApiResponse.Error("User service is unavailable."));
                return;
            }
            catch (TaskCanceledException)
            {
                await WriteErrorAsync(context, 504, ApiResponse.Error("User service timed out."));
                return;
            }
            catch (Exception)
            {
                await WriteErrorAsync(context, 500, ApiResponse.ServerError());
                return;
            }

            if (user == null)
            {
                await WriteErrorAsync(context, 401, ApiResponse.Unauthorized("User no longer exists"));
                return;
            }

            if (user.Id != parsedUserId)
            {
                await WriteErrorAsync(context, 401, ApiResponse.TokenInvalid());
                return;
            }

            if (!user.Enabled)
            {
                await WriteErrorAsync(context, 403, ApiResponse.Forbidden("Your account has been disabled"));
                return;
            }

            var record = user.Records.FirstOrDefault();
            if (record != null)
            {
                if (record.Blocked)
                {
                    var reason = record.BlockedReason ?? "Account has been blocked";
                    var until  = record.BlockedUntil != null ? $" until {record.BlockedUntil}" : "";
                    await WriteErrorAsync(context, 403, ApiResponse.Forbidden($"{reason}{until}"));
                    return;
                }

                if (record.Locked)
                {
                    var lockedAt = record.LockedAt.HasValue
                        ? $" since {record.LockedAt.Value:yyyy-MM-dd HH:mm} UTC"
                        : "";
                    await WriteErrorAsync(context, 403, ApiResponse.Forbidden($"Account is locked{lockedAt}"));
                    return;
                }
            }

            context.Items["VerifiedUser"] = user;
            context.Items["RequestId"]    = requestId;

            await _next(context);
        }

        private static async Task WriteErrorAsync(HttpContext context, int statusCode, object body)
        {
            context.Response.StatusCode  = statusCode;
            context.Response.ContentType = "application/json";
            await context.Response.WriteAsync(JsonSerializer.Serialize(body, new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            }));
        }
    }
}