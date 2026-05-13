using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Diagnostics;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;
using resiliences_service.Models;

namespace resiliences_service.Configs
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options)
            : base(options) { }

        public DbSet<Beneficiary>            Beneficiaries           { get; set; }
        public DbSet<BlackListedWallet>      BlackListedWallets      { get; set; }
        public DbSet<History>                Histories               { get; set; }
        public DbSet<UserBankList>           UserBankLists           { get; set; }
        public DbSet<DebtCollector>          DebtCollectors          { get; set; }
        public DbSet<WalletMaintenance>      WalletMaintenances      { get; set; }
        public DbSet<MaintenanceFeeHistory>  MaintenanceFeeHistories { get; set; }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            optionsBuilder.ConfigureWarnings(w =>
                w.Ignore(RelationalEventId.PendingModelChangesWarning));
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
            var utcConverter = new ValueConverter<DateTime, DateTime>(
                v => v.Kind == DateTimeKind.Utc ? v : DateTime.SpecifyKind(v, DateTimeKind.Utc),
                v => DateTime.SpecifyKind(v, DateTimeKind.Utc)
            );

            var utcNullableConverter = new ValueConverter<DateTime?, DateTime?>(
                v => v.HasValue ? (v.Value.Kind == DateTimeKind.Utc ? v : DateTime.SpecifyKind(v.Value, DateTimeKind.Utc)) : v,
                v => v.HasValue ? DateTime.SpecifyKind(v.Value, DateTimeKind.Utc) : v
            );

            foreach (var entityType in modelBuilder.Model.GetEntityTypes())
            {
                foreach (var property in entityType.GetProperties())
                {
                    if (property.ClrType == typeof(DateTime))
                        property.SetValueConverter(utcConverter);

                    if (property.ClrType == typeof(DateTime?))
                        property.SetValueConverter(utcNullableConverter);
                }
            }
            
            modelBuilder.Entity<Beneficiary>()
                .HasIndex(b => new { b.UserId, b.BeneficiaryType })
                .HasDatabaseName("idx_user_beneficiary");

            modelBuilder.Entity<Beneficiary>()
                .HasIndex(b => b.AccountNumber)
                .HasDatabaseName("idx_account_number");

            modelBuilder.Entity<Beneficiary>()
                .HasIndex(b => b.RecipientUsername)
                .HasDatabaseName("idx_recipient_username");

            modelBuilder.Entity<UserBankList>()
                .HasIndex(u => u.AccountNumber)
                .IsUnique();

            modelBuilder.Entity<BlackListedWallet>()
                .HasIndex(b => b.Timestamp);

            modelBuilder.Entity<History>()
                .HasKey(h => h.Id);

            modelBuilder.Entity<History>()
                .Property(h => h.Type)
                .HasConversion<string>()
                .HasColumnType("varchar(50)");

            modelBuilder.Entity<DebtCollector>()
                .Property(d => d.DebtStatus)
                .HasConversion<string>()
                .HasColumnType("varchar(50)");

            modelBuilder.Entity<DebtCollector>()
                .Property(d => d.CurrencyType)
                .HasConversion<string>()
                .HasColumnType("varchar(10)");

            modelBuilder.Entity<DebtCollector>()
                .HasIndex(d => d.UserId)
                .HasDatabaseName("idx_debt_collector_user_id");

            modelBuilder.Entity<WalletMaintenance>()
                .Property(w => w.Status)
                .HasConversion<string>()
                .HasColumnType("varchar(50)");

            modelBuilder.Entity<WalletMaintenance>()
                .Property(w => w.CurrencyType)
                .HasConversion<string>()
                .HasColumnType("varchar(10)");

            modelBuilder.Entity<WalletMaintenance>()
                .HasIndex(w => new { w.UserId, w.CurrencyType })
                .IsUnique()
                .HasDatabaseName("idx_wallet_maintenance_user_currency");

            modelBuilder.Entity<MaintenanceFeeHistory>()
                .Property(m => m.Status)
                .HasConversion<string>()
                .HasColumnType("varchar(50)");

            modelBuilder.Entity<MaintenanceFeeHistory>()
                .Property(m => m.CurrencyType)
                .HasConversion<string>()
                .HasColumnType("varchar(10)");

            modelBuilder.Entity<MaintenanceFeeHistory>()
                .HasIndex(m => m.UserId)
                .HasDatabaseName("idx_maintenance_fee_history_user_id");

            modelBuilder.Entity<MaintenanceFeeHistory>()
                .HasIndex(m => m.AttemptedOn)
                .HasDatabaseName("idx_maintenance_fee_history_attempted_on");
        }

        public override Task<int> SaveChangesAsync(CancellationToken cancellationToken = default)
        {
            var entries = ChangeTracker.Entries()
                .Where(e => e.State == EntityState.Added || e.State == EntityState.Modified);

            foreach (var entry in entries)
            {
                if (entry.Properties.Any(p => p.Metadata.Name == "UpdatedOn"))
                    entry.Property("UpdatedOn").CurrentValue = DateTime.UtcNow;

                if (entry.State == EntityState.Added &&
                    entry.Properties.Any(p => p.Metadata.Name == "CreatedOn"))
                    entry.Property("CreatedOn").CurrentValue = DateTime.UtcNow;
            }

            return base.SaveChangesAsync(cancellationToken);
        }
    }
}