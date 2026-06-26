using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Diagnostics;
using System.Text.Json;
using System.Text.Json.Serialization;
using StackExchange.Redis;
using FluentValidation.AspNetCore;
using resiliences_service.Clients;
using resiliences_service.Configs;
using resiliences_service.Middleware;
using resiliences_service.Repositories;
using resiliences_service.Resopones;
using resiliences_service.Services;
using resiliences_service.interfaces;
using resiliences_service.Workers;
var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSignalR(options =>
{
    options.EnableDetailedErrors          = builder.Environment.IsDevelopment();
    options.MaximumReceiveMessageSize     = 1024 * 1024 * 10;
    options.StreamBufferCapacity          = 100;
    options.ClientTimeoutInterval         = TimeSpan.FromSeconds(30);
    options.HandshakeTimeout              = TimeSpan.FromSeconds(15);
    options.KeepAliveInterval             = TimeSpan.FromSeconds(10);
    options.MaximumParallelInvocationsPerClient = 10;
})
.AddJsonProtocol(options =>
{
    options.PayloadSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
    options.PayloadSerializerOptions.WriteIndented        = false;
});

builder.Services.AddCors(options =>
{
    options.AddPolicy("SignalRCors", policy =>
        policy.SetIsOriginAllowed(_ => true)
              .AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials()
              .WithExposedHeaders("X-SignalR-Connection-Id"));
});

builder.Services.AddDbContext<AppDbContext>(options =>
{
    var connString = builder.Configuration.GetConnectionString("DefaultConnection")
        ?? throw new InvalidOperationException("DefaultConnection is missing in configuration");

    options.UseNpgsql(connString);
    options.ConfigureWarnings(w => w.Ignore(RelationalEventId.PendingModelChangesWarning));
});


builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter());
        options.JsonSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
    });

builder.Services.AddSingleton<IConnectionMultiplexer>(
    ConnectionMultiplexer.Connect(builder.Configuration["Redis:ConnectionString"]!));

builder.Services.AddStackExchangeRedisCache(options =>
{
    options.Configuration = builder.Configuration["Redis:ConnectionString"];
    options.InstanceName  = "resiliences_service:";
});

builder.Services.AddFluentValidationAutoValidation();
builder.Services.AddFluentValidationClientsideAdapters();

builder.Services.AddScoped<IHistoryRepository, HistoryRepository>();
builder.Services.AddScoped<IHistoryService, HistoryService>();
builder.Services.AddSingleton<IHistoryCacheService, HistoryCacheService>();

var jwtSettings = builder.Configuration.GetSection("JwtSettings");
var secretKey   = jwtSettings["SecretKey"] ?? throw new InvalidOperationException("JWT SecretKey missing");

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme    = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer           = true,
        ValidateAudience         = true,
        ValidateLifetime         = true,
        ValidateIssuerSigningKey = true,
        ValidIssuer              = jwtSettings["Issuer"],
        ValidAudience            = jwtSettings["Audience"],
        IssuerSigningKey         = new SymmetricSecurityKey(Convert.FromBase64String(secretKey)),
        ClockSkew                = TimeSpan.Zero
    };

    options.Events = new JwtBearerEvents
    {
        OnChallenge = async context =>
        {
            context.HandleResponse();
            context.Response.StatusCode  = 401;
            context.Response.ContentType = "application/json";

            var error = context.AuthenticateFailure;
            object body = error is SecurityTokenExpiredException ? ApiResponse.TokenExpired()
                        : error is SecurityTokenException        ? ApiResponse.TokenInvalid()
                        : ApiResponse.Unauthorized("No token provided or token is missing");

            await context.Response.WriteAsync(JsonSerializer.Serialize(body,
                new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase }));
        },

        OnForbidden = async context =>
        {
            context.Response.StatusCode  = 403;
            context.Response.ContentType = "application/json";
            await context.Response.WriteAsync(JsonSerializer.Serialize(
                ApiResponse.Forbidden("You do not have permission to access this resource"),
                new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase }));
        },

        OnMessageReceived = context =>
        {
            var accessToken = context.Request.Query["access_token"];
            if (!string.IsNullOrEmpty(accessToken) &&
                context.HttpContext.Request.Path.StartsWithSegments("/hubs"))
                context.Token = accessToken;
            return Task.CompletedTask;
        }
    };
});


builder.Services.Configure<FirewallSettings>(builder.Configuration.GetSection("FirewallSettings"));
builder.Services.AddSingleton<AttackPatternDetector>();
builder.Services.AddScoped<JwtService>();
builder.Services.AddHttpContextAccessor();
builder.Services.AddHostedService<MaintenanceWorker>();
builder.Services.AddScoped<IUserBankRepository, UserBankRepository>();
builder.Services.AddScoped<IUserBankService, UserBankService>();
builder.Services.AddScoped<IBeneficiaryRepository, BeneficiaryRepository>();
builder.Services.AddScoped<IBeneficiaryService, BeneficiaryService>();
builder.Services.AddScoped<IBlackListedWalletRepository, BlackListedWalletRepository>();
builder.Services.AddScoped<IBlackListedWalletService, BlackListedWalletService>();
builder.Services.AddScoped<IWalletMaintenanceRepository, WalletMaintenanceRepository>();
builder.Services.AddScoped<IMaintenanceFeeHistoryRepository, MaintenanceFeeHistoryRepository>();
builder.Services.AddScoped<IMaintenanceService, MaintenanceService>();

// Investment & Target Savings
builder.Services.AddScoped<IInvestmentRepository, InvestmentRepository>();
builder.Services.AddScoped<IInvestmentService, InvestmentService>();
builder.Services.AddScoped<ITargetSavingsRepository, TargetSavingsRepository>();
builder.Services.AddScoped<ITargetSavingsService, TargetSavingsService>();
builder.Services.AddHostedService<InvestmentMaturityWorker>();

builder.Services.AddHttpClient<UserServiceClient>(client =>
{
    client.BaseAddress = new Uri(builder.Configuration["ServiceUrls:UserService"]
        ?? throw new InvalidOperationException("ServiceUrls:UserService is missing"));
    client.DefaultRequestHeaders.Add("User-Agent", "resiliences-service/1.0");
});

builder.Services.AddHttpClient<WalletServiceClient>(client =>
{
    client.BaseAddress = new Uri(builder.Configuration["ServiceUrls:WalletService"]
        ?? throw new InvalidOperationException("ServiceUrls:WalletService is missing"));
    client.DefaultRequestHeaders.Add("User-Agent", "resiliences-service/1.0");
});

builder.Services.AddHttpClient<RevenueServiceClient>(client =>
{
    client.BaseAddress = new Uri(builder.Configuration["ServiceUrls:RevenueService"]
        ?? throw new InvalidOperationException("ServiceUrls:RevenueService is missing"));
    client.DefaultRequestHeaders.Add("User-Agent", "resiliences-service/1.0");
});

builder.Services.AddHttpClient<NotificationServiceClient>(client =>
{
    client.BaseAddress = new Uri(builder.Configuration["ServiceUrls:NotificationService"]
        ?? throw new InvalidOperationException("ServiceUrls:NotificationService is missing"));
    client.DefaultRequestHeaders.Add("User-Agent", "resiliences-service/1.0");
});

builder.Services.AddHttpClient("Paystack", client =>
{
    client.BaseAddress = new Uri(builder.Configuration["Paystack:BaseUrl"] ?? "https://api.paystack.co");
    client.DefaultRequestHeaders.Add("Authorization", $"Bearer {builder.Configuration["Paystack:ApiKey"]}");
});

builder.Services.AddMemoryCache(options =>
{
    options.SizeLimit                = 1024 * 1024 * 512;
    options.CompactionPercentage     = 0.25;
    options.ExpirationScanFrequency  = TimeSpan.FromMinutes(5);
});

builder.Services.AddResponseCompression(options => options.EnableForHttps = true);
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

using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    db.Database.Migrate();

    // Safety-net: create Investment & TargetSavings tables if the migration
    // hasn't been compiled into the running image yet (e.g. hot-patched code).
    db.Database.ExecuteSqlRaw(@"
        CREATE TABLE IF NOT EXISTS ""Investments"" (
            ""Id""             bigserial        NOT NULL,
            ""UserId""         bigint           NOT NULL,
            ""WalletId""       bigint           NOT NULL,
            ""CurrencyCode""   varchar(10)      NOT NULL,
            ""Principal""      decimal(18,4)    NOT NULL,
            ""ReturnRate""     decimal(5,2)     NOT NULL DEFAULT 0,
            ""ExpectedProfit"" decimal(18,4)    NOT NULL DEFAULT 0,
            ""TotalPayout""    decimal(18,4)    NOT NULL DEFAULT 0,
            ""Duration""       varchar(20)      NOT NULL,
            ""DurationDays""   integer          NOT NULL DEFAULT 0,
            ""StartDate""      timestamptz      NOT NULL DEFAULT NOW(),
            ""MaturityDate""   timestamptz      NOT NULL DEFAULT NOW(),
            ""Status""         varchar(20)      NOT NULL DEFAULT 'ACTIVE',
            ""PaidOutAt""      timestamptz      NULL,
            ""ReferenceId""    varchar(50)      NULL,
            ""CreatedOn""      timestamptz      NOT NULL DEFAULT NOW(),
            ""UpdatedOn""      timestamptz      NOT NULL DEFAULT NOW(),
            CONSTRAINT ""PK_Investments"" PRIMARY KEY (""Id"")
        );
        CREATE INDEX IF NOT EXISTS idx_investment_user_id      ON ""Investments""(""UserId"");
        CREATE INDEX IF NOT EXISTS idx_investment_status       ON ""Investments""(""Status"");
        CREATE INDEX IF NOT EXISTS idx_investment_maturity_date ON ""Investments""(""MaturityDate"");

        CREATE TABLE IF NOT EXISTS ""TargetSavings"" (
            ""Id""           bigserial     NOT NULL,
            ""UserId""       bigint        NOT NULL,
            ""WalletId""     bigint        NOT NULL,
            ""CurrencyCode"" varchar(10)   NOT NULL,
            ""GoalName""     varchar(100)  NOT NULL,
            ""Description""  varchar(500)  NULL,
            ""TargetAmount"" decimal(18,4) NOT NULL,
            ""SavedAmount""  decimal(18,4) NOT NULL DEFAULT 0,
            ""TargetDate""   timestamptz   NULL,
            ""Status""       varchar(20)   NOT NULL DEFAULT 'ACTIVE',
            ""GoalIcon""     varchar(10)   NULL,
            ""CompletedAt""  timestamptz   NULL,
            ""WithdrawnAt""  timestamptz   NULL,
            ""CreatedOn""    timestamptz   NOT NULL DEFAULT NOW(),
            ""UpdatedOn""    timestamptz   NOT NULL DEFAULT NOW(),
            CONSTRAINT ""PK_TargetSavings"" PRIMARY KEY (""Id"")
        );
        CREATE INDEX IF NOT EXISTS idx_target_savings_user_id     ON ""TargetSavings""(""UserId"");
        CREATE INDEX IF NOT EXISTS idx_target_savings_user_status ON ""TargetSavings""(""UserId"", ""Status"");
    ");
}

app.UseAuthentication();
app.UseAuthorization();
app.UseMiddleware<SecurityMiddleware>();
app.MapControllers();
app.Run();