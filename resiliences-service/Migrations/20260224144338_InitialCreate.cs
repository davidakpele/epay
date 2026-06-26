using System;
using Microsoft.EntityFrameworkCore.Migrations;
using Npgsql.EntityFrameworkCore.PostgreSQL.Metadata;

#nullable disable

namespace resiliences_service.Migrations
{
    /// <inheritdoc />
    public partial class InitialCreate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "beneficiaries",
                columns: table => new
                {
                    Id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    UserId = table.Column<long>(type: "bigint", nullable: false),
                    BeneficiaryType = table.Column<string>(type: "varchar(20)", nullable: false),
                    BeneficiaryName = table.Column<string>(type: "varchar(255)", nullable: false),
                    Currency = table.Column<string>(type: "varchar(10)", nullable: false),
                    AccountNumber = table.Column<string>(type: "varchar(20)", nullable: true),
                    AccountName = table.Column<string>(type: "varchar(255)", nullable: true),
                    BankCode = table.Column<string>(type: "varchar(50)", nullable: true),
                    BankName = table.Column<string>(type: "varchar(255)", nullable: true),
                    RecipientUsername = table.Column<string>(type: "varchar(255)", nullable: true),
                    IsActive = table.Column<bool>(type: "boolean", nullable: false),
                    CreatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    UpdatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_beneficiaries", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "BlackListedWallets",
                columns: table => new
                {
                    Id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    WalletId = table.Column<long>(type: "bigint", nullable: false),
                    BankBannedReason = table.Column<string>(type: "varchar(200)", nullable: false),
                    IsBlock = table.Column<bool>(type: "boolean", nullable: false),
                    Timestamp = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    UpdatedAt = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_BlackListedWallets", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "Histories",
                columns: table => new
                {
                    Id = table.Column<string>(type: "char(36)", maxLength: 36, nullable: false),
                    WalletId = table.Column<decimal>(type: "numeric(20,0)", nullable: false),
                    UserId = table.Column<decimal>(type: "numeric(20,0)", nullable: false),
                    SessionId = table.Column<string>(type: "text", nullable: true),
                    TransactionId = table.Column<string>(type: "text", nullable: true),
                    referenceNo = table.Column<string>(type: "text", nullable: true),
                    TerminalId = table.Column<string>(type: "text", nullable: true),
                    ErId = table.Column<string>(type: "text", nullable: true),
                    AccountHolder = table.Column<string>(type: "text", nullable: true),
                    PreviousBalance = table.Column<double>(type: "double precision", nullable: false),
                    AvailableBalance = table.Column<double>(type: "double precision", nullable: false),
                    Amount = table.Column<double>(type: "double precision", nullable: false),
                    Type = table.Column<string>(type: "varchar(50)", nullable: true),
                    Description = table.Column<string>(type: "text", nullable: true),
                    Message = table.Column<string>(type: "text", nullable: true),
                    CurrencyType = table.Column<string>(type: "varchar(10)", nullable: false),
                    Status = table.Column<string>(type: "text", nullable: true),
                    ip_address = table.Column<string>(type: "character varying(45)", maxLength: 45, nullable: true),
                    Timestamp = table.Column<DateTime>(type: "timestamp with time zone", nullable: true),
                    CreatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    UpdatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Histories", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "UserBankLists",
                columns: table => new
                {
                    Id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    BankCode = table.Column<string>(type: "text", nullable: true),
                    BankName = table.Column<string>(type: "text", nullable: true),
                    AccountHolderName = table.Column<string>(type: "text", nullable: true),
                    AccountNumber = table.Column<string>(type: "text", nullable: false),
                    UserId = table.Column<long>(type: "bigint", nullable: false),
                    CreatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    UpdatedOn = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_UserBankLists", x => x.Id);
                });

            migrationBuilder.CreateIndex(
                name: "idx_account_number",
                table: "beneficiaries",
                column: "AccountNumber");

            migrationBuilder.CreateIndex(
                name: "idx_recipient_username",
                table: "beneficiaries",
                column: "RecipientUsername");

            migrationBuilder.CreateIndex(
                name: "idx_user_beneficiary",
                table: "beneficiaries",
                columns: new[] { "UserId", "BeneficiaryType" });

            migrationBuilder.CreateIndex(
                name: "IX_BlackListedWallets_Timestamp",
                table: "BlackListedWallets",
                column: "Timestamp");

            migrationBuilder.CreateIndex(
                name: "IX_UserBankLists_AccountNumber",
                table: "UserBankLists",
                column: "AccountNumber",
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "beneficiaries");

            migrationBuilder.DropTable(
                name: "BlackListedWallets");

            migrationBuilder.DropTable(
                name: "Histories");

            migrationBuilder.DropTable(
                name: "UserBankLists");
        }
    }
}
