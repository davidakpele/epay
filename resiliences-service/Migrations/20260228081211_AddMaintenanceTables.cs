using System;
using Microsoft.EntityFrameworkCore.Migrations;
using Npgsql.EntityFrameworkCore.PostgreSQL.Metadata;

#nullable disable

namespace resiliences_service.Migrations
{
    /// <inheritdoc />
    public partial class AddMaintenanceTables : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "DebtCollectors",
                columns: table => new
                {
                    Id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    UserId = table.Column<long>(type: "bigint", nullable: false),
                    Amount = table.Column<decimal>(type: "numeric", nullable: false),
                    DueAmount = table.Column<decimal>(type: "numeric", nullable: false),
                    DebtStatus = table.Column<string>(type: "varchar(50)", nullable: true),
                    Description = table.Column<string>(type: "text", nullable: true),
                    CurrencyType = table.Column<string>(type: "varchar(10)", nullable: false),
                    CreatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: true),
                    UpdatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_DebtCollectors", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "MaintenanceFeeHistories",
                columns: table => new
                {
                    Id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    UserId = table.Column<long>(type: "bigint", nullable: false),
                    CurrencyType = table.Column<string>(type: "varchar(10)", nullable: false),
                    FeeAmount = table.Column<decimal>(type: "numeric", nullable: false),
                    Status = table.Column<string>(type: "varchar(50)", nullable: false),
                    AttemptedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: true),
                    PaidOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: true),
                    Reason = table.Column<string>(type: "text", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_MaintenanceFeeHistories", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "WalletMaintenances",
                columns: table => new
                {
                    Id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    UserId = table.Column<long>(type: "bigint", nullable: false),
                    CurrencyType = table.Column<string>(type: "varchar(10)", nullable: false),
                    Balance = table.Column<decimal>(type: "numeric", nullable: false),
                    Status = table.Column<string>(type: "varchar(50)", nullable: false),
                    LastCharged = table.Column<DateTime>(type: "timestamp with time zone", nullable: true),
                    CreatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: true),
                    UpdatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_WalletMaintenances", x => x.Id);
                });

            migrationBuilder.CreateIndex(
                name: "idx_debt_collector_user_id",
                table: "DebtCollectors",
                column: "UserId");

            migrationBuilder.CreateIndex(
                name: "idx_maintenance_fee_history_attempted_on",
                table: "MaintenanceFeeHistories",
                column: "AttemptedOn");

            migrationBuilder.CreateIndex(
                name: "idx_maintenance_fee_history_user_id",
                table: "MaintenanceFeeHistories",
                column: "UserId");

            migrationBuilder.CreateIndex(
                name: "idx_wallet_maintenance_user_currency",
                table: "WalletMaintenances",
                columns: new[] { "UserId", "CurrencyType" },
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "DebtCollectors");

            migrationBuilder.DropTable(
                name: "MaintenanceFeeHistories");

            migrationBuilder.DropTable(
                name: "WalletMaintenances");
        }
    }
}
