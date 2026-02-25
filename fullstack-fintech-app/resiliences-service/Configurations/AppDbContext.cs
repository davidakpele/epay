using Microsoft.EntityFrameworkCore;
using resiliences_service.Models;
using resiliences_service.Enums;

namespace resiliences_service.Configs
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) 
            : base(options) { }

        public DbSet<Beneficiary> Beneficiaries { get; set; }
        public DbSet<BlackListedWallet> BlackListedWallets { get; set; }
        public DbSet<History> Histories { get; set; }
        public DbSet<UserBankList> UserBankLists { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

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

            // Store enum as string in DB
            modelBuilder.Entity<History>()
                .Property(h => h.Type)
                .HasConversion<string>()
                .HasColumnType("varchar(50)");
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