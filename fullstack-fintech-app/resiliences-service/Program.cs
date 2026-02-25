using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using System.Text.Json;
using resiliences_service.Configs;
using resiliences_service.Clients;
using resiliences_service.Middleware;
using resiliences_service.Services;
using FluentValidation.AspNetCore;
using Microsoft.EntityFrameworkCore;
using resiliences_service.Resopones;
using resiliences_service.Repositories;
using resiliences_service.interfaces;
using System.Text.Json.Serialization;
using StackExchange.Redis;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSignalR(options =>
{
    options.EnableDetailedErrors = builder.Environment.IsDevelopment();
    options.MaximumReceiveMessageSize = 1024 * 1024 * 10;
    options.StreamBufferCapacity = 100;
    options.ClientTimeoutInterval = TimeSpan.FromSeconds(30);
    options.HandshakeTimeout = TimeSpan.FromSeconds(15);
    options.KeepAliveInterval = TimeSpan.FromSeconds(10);
    options.MaximumParallelInvocationsPerClient = 10;
})
.AddJsonProtocol(options =>
{
    options.PayloadSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
    options.PayloadSerializerOptions.WriteIndented = false;
});

builder.Services.AddCors(options =>
{
    options.AddPolicy("SignalRCors", policy =>
    {
        policy
            .SetIsOriginAllowed(_ => true)
            .AllowAnyHeader()
            .AllowAnyMethod()
            .AllowCredentials()
            .WithExposedHeaders("X-SignalR-Connection-Id");
    });
});
builder.Services.AddDbContext<AppDbContext>(options =>
{
    var connString = builder.Configuration.GetConnectionString("DefaultConnection")
        ?? throw new InvalidOperationException("DefaultConnection is missing in configuration");

    options.UseNpgsql(connString);
});
builder.Services.AddControllers()
.AddJsonOptions(options =>
{
    options.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter());
    options.JsonSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
});
builder.Services.AddStackExchangeRedisCache(options =>
{
    options.Configuration = builder.Configuration["Redis:ConnectionString"];
    options.InstanceName = "resiliences_service:";
});
builder.Services.AddSingleton<IConnectionMultiplexer>(
    ConnectionMultiplexer.Connect(builder.Configuration["Redis:ConnectionString"]!));

builder.Services.AddStackExchangeRedisCache(options =>
{
    options.Configuration = builder.Configuration["Redis:ConnectionString"];
    options.InstanceName = "resiliences_service:";
});
builder.Services.AddScoped<IHistoryRepository, HistoryRepository>();
builder.Services.AddScoped<IHistoryService, HistoryService>();
builder.Services.AddControllers();
builder.Services.AddFluentValidationAutoValidation();
builder.Services.AddFluentValidationClientsideAdapters();
builder.Services.AddScoped<IHistoryRepository, HistoryRepository>();
builder.Services.AddScoped<IHistoryService, HistoryService>();
builder.Services.AddSingleton<IHistoryCacheService, HistoryCacheService>();

var jwtSettings = builder.Configuration.GetSection("JwtSettings");
var secretKey = jwtSettings["SecretKey"] ?? throw new InvalidOperationException("JWT SecretKey missing");
builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidateLifetime = true,
        ValidateIssuerSigningKey = true,
        ValidIssuer = jwtSettings["Issuer"],
        ValidAudience = jwtSettings["Audience"],
        IssuerSigningKey = new SymmetricSecurityKey(Convert.FromBase64String(secretKey)),
        ClockSkew = TimeSpan.Zero
    };

    options.Events = new JwtBearerEvents
    {
        OnChallenge = async context =>
        {
            context.HandleResponse(); 

            context.Response.StatusCode  = 401;
            context.Response.ContentType = "application/json";

            var error = context.AuthenticateFailure;
            object body;

            if (error is SecurityTokenExpiredException)
                body = ApiResponse.TokenExpired();
            else if (error is SecurityTokenException)
                body = ApiResponse.TokenInvalid();
            else
                body = ApiResponse.Unauthorized("No token provided or token is missing");

            await context.Response.WriteAsync(JsonSerializer.Serialize(body, new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            }));
        },

        OnForbidden = async context =>
        {
            context.Response.StatusCode  = 403;
            context.Response.ContentType = "application/json";

            await context.Response.WriteAsync(JsonSerializer.Serialize(
                ApiResponse.Forbidden("You do not have permission to access this resource"),
                new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase }
            ));
        },

        OnMessageReceived = context =>
        {
            var accessToken = context.Request.Query["access_token"];
            var path = context.HttpContext.Request.Path;

            if (!string.IsNullOrEmpty(accessToken) && path.StartsWithSegments("/hubs"))
                context.Token = accessToken;

            return Task.CompletedTask;
        }
    };
});

builder.Services.Configure<FirewallSettings>(
builder.Configuration.GetSection("FirewallSettings"));
builder.Services.AddSingleton<AttackPatternDetector>();
builder.Services.AddScoped<JwtService>();
builder.Services.AddHttpContextAccessor();
builder.Services.AddScoped<IUserBankRepository, UserBankRepository>();
builder.Services.AddScoped<IUserBankService, UserBankService>();
builder.Services.AddScoped<IBeneficiaryRepository, BeneficiaryRepository>();
builder.Services.AddScoped<IBeneficiaryService, BeneficiaryService>();
builder.Services.AddScoped<IBlackListedWalletRepository, BlackListedWalletRepository>();
builder.Services.AddScoped<IBlackListedWalletService, BlackListedWalletService>();
builder.Services.AddMemoryCache(options =>
{
    options.SizeLimit = 1024 * 1024 * 512;
    options.CompactionPercentage = 0.25;
    options.ExpirationScanFrequency = TimeSpan.FromMinutes(5);
});

builder.Services.AddResponseCompression(options =>
{
    options.EnableForHttps = true;
});

builder.Services.AddHttpClient<UserServiceClient>(client =>
{
    var baseUrl = builder.Configuration["UserService:BaseUrl"]
        ?? throw new InvalidOperationException("UserService:BaseUrl is missing in configuration");

    client.BaseAddress = new Uri(baseUrl);
    client.DefaultRequestHeaders.Add("User-Agent", "resiliences-service/1.0");
});
builder.Services.AddHttpClient("Paystack", client =>
{
    client.BaseAddress = new Uri(builder.Configuration["Paystack:BaseUrl"]
        ?? "https://api.paystack.co");
    client.DefaultRequestHeaders.Add("Authorization",
        $"Bearer {builder.Configuration["Paystack:ApiKey"]}");
});
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

app.UseResponseCompression();
app.UseCors("SignalRCors");
app.UseMiddleware<ExceptionMiddleware>();
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseAuthentication();
app.UseAuthorization();
app.UseMiddleware<SecurityMiddleware>();
app.MapControllers();
app.Run();