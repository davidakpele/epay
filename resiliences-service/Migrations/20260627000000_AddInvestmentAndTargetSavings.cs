using System;
using Microsoft.EntityFrameworkCore.Migrations;
using Npgsql.EntityFrameworkCore.PostgreSQL.Metadata;

#nullable disable

namespace resiliences_service.Migrations
{
    /// <inheritdoc />
    public partial class AddInvestmentAndTargetSavings : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            // ── Investments ────────────────────────────────────────────────────
            migrationBuilder.CreateTable(
                name: "Investments",
                columns: table => new
                {
                    Id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    UserId = table.Column<long>(type: "bigint", nullable: false),
                    WalletId = table.Column<long>(type: "bigint", nullable: false),
                    CurrencyCode = table.Column<string>(type: "varchar(10)", nullable: false),
                    Principal = table.Column<decimal>(type: "decimal(18,4)", nullable: false),
                    ReturnRate = table.Column<decimal>(type: "decimal(5,2)", nullable: false),
                    ExpectedProfit = table.Column<decimal>(type: "decimal(18,4)", nullable: false),
                    TotalPayout = table.Column<decimal>(type: "decimal(18,4)", nullable: false),
                    Duration = table.Column<string>(type: "varchar(20)", nullable: false),
                    DurationDays = table.Column<int>(type: "integer", nullable: false),
                    StartDate = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    MaturityDate = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    Status = table.Column<string>(type: "varchar(20)", nullable: false),
                    PaidOutAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: true),
                    ReferenceId = table.Column<string>(type: "varchar(50)", nullable: true),
                    CreatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: false, defaultValueSql: "NOW()"),
                    UpdatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: false, defaultValueSql: "NOW()")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Investments", x => x.Id);
                });

            migrationBuilder.CreateIndex(
                name: "idx_investment_user_id",
                table: "Investments",
                column: "UserId");

            migrationBuilder.CreateIndex(
                name: "idx_investment_status",
                table: "Investments",
                column: "Status");

            migrationBuilder.CreateIndex(
                name: "idx_investment_maturity_date",
                table: "Investments",
                column: "MaturityDate");

            // ── TargetSavings ──────────────────────────────────────────────────
            migrationBuilder.CreateTable(
                name: "TargetSavings",
                columns: table => new
                {
                    Id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    UserId = table.Column<long>(type: "bigint", nullable: false),
                    WalletId = table.Column<long>(type: "bigint", nullable: false),
                    CurrencyCode = table.Column<string>(type: "varchar(10)", nullable: false),
                    GoalName = table.Column<string>(type: "varchar(100)", nullable: false),
                    Description = table.Column<string>(type: "varchar(500)", nullable: true),
                    TargetAmount = table.Column<decimal>(type: "decimal(18,4)", nullable: false),
                    SavedAmount = table.Column<decimal>(type: "decimal(18,4)", nullable: false, defaultValue: 0m),
                    TargetDate = table.Column<DateTime>(type: "timestamp with time zone", nullable: true),
                    Status = table.Column<string>(type: "varchar(20)", nullable: false),
                    GoalIcon = table.Column<string>(type: "varchar(10)", nullable: true),
                    CompletedAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: true),
                    WithdrawnAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: true),
                    CreatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: false, defaultValueSql: "NOW()"),
                    UpdatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: false, defaultValueSql: "NOW()")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_TargetSavings", x => x.Id);
                });

            migrationBuilder.CreateIndex(
                name: "idx_target_savings_user_id",
                table: "TargetSavings",
                column: "UserId");

            migrationBuilder.CreateIndex(
                name: "idx_target_savings_user_status",
                table: "TargetSavings",
                columns: new[] { "UserId", "Status" });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(name: "Investments");
            migrationBuilder.DropTable(name: "TargetSavings");
        }
    }
}
