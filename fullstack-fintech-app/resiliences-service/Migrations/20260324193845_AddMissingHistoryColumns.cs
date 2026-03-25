using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace resiliences_service.Migrations
{
    /// <inheritdoc />
    public partial class AddMissingHistoryColumns : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "Amount",
                table: "Histories");

            migrationBuilder.AlterColumn<string>(
                name: "Status",
                table: "Histories",
                type: "varchar(20)",
                nullable: true,
                oldClrType: typeof(string),
                oldType: "text",
                oldNullable: true);

            migrationBuilder.AlterColumn<decimal>(
                name: "PreviousBalance",
                table: "Histories",
                type: "numeric(18,4)",
                nullable: false,
                oldClrType: typeof(double),
                oldType: "double precision");

            migrationBuilder.AlterColumn<decimal>(
                name: "AvailableBalance",
                table: "Histories",
                type: "numeric(18,4)",
                nullable: false,
                oldClrType: typeof(double),
                oldType: "double precision");

            migrationBuilder.AddColumn<string>(
                name: "AdminNote",
                table: "Histories",
                type: "character varying(500)",
                maxLength: 500,
                nullable: true);

            migrationBuilder.AddColumn<bool>(
                name: "AmlFlag",
                table: "Histories",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<DateTime>(
                name: "ApprovalTimestamp",
                table: "Histories",
                type: "timestamp with time zone",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "ApprovedBy",
                table: "Histories",
                type: "character varying(36)",
                maxLength: 36,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "BankAccountNumber",
                table: "Histories",
                type: "character varying(30)",
                maxLength: 30,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "BankCode",
                table: "Histories",
                type: "character varying(20)",
                maxLength: 20,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "Category",
                table: "Histories",
                type: "varchar(30)",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "Channel",
                table: "Histories",
                type: "varchar(20)",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "ComplianceNote",
                table: "Histories",
                type: "character varying(500)",
                maxLength: 500,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "CounterpartyAccountHolder",
                table: "Histories",
                type: "character varying(20)",
                maxLength: 20,
                nullable: true);

            migrationBuilder.AddColumn<decimal>(
                name: "CounterpartyUserId",
                table: "Histories",
                type: "numeric(20,0)",
                nullable: true);

            migrationBuilder.AddColumn<decimal>(
                name: "CounterpartyWalletId",
                table: "Histories",
                type: "numeric(20,0)",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "DebitCredit",
                table: "Histories",
                type: "varchar(10)",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "DeviceId",
                table: "Histories",
                type: "character varying(100)",
                maxLength: 100,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "DisputeReference",
                table: "Histories",
                type: "character varying(100)",
                maxLength: 100,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "DisputeStatus",
                table: "Histories",
                type: "varchar(30)",
                nullable: true);

            migrationBuilder.AddColumn<decimal>(
                name: "ExchangeRate",
                table: "Histories",
                type: "numeric(18,8)",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "ExternalReference",
                table: "Histories",
                type: "character varying(100)",
                maxLength: 100,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "FailureReason",
                table: "Histories",
                type: "character varying(500)",
                maxLength: 500,
                nullable: true);

            migrationBuilder.AddColumn<decimal>(
                name: "FeeAmount",
                table: "Histories",
                type: "numeric(18,4)",
                nullable: false,
                defaultValue: 0m);

            migrationBuilder.AddColumn<string>(
                name: "GeoLocation",
                table: "Histories",
                type: "character varying(100)",
                maxLength: 100,
                nullable: true);

            migrationBuilder.AddColumn<decimal>(
                name: "GrossAmount",
                table: "Histories",
                type: "numeric(18,4)",
                nullable: false,
                defaultValue: 0m);

            migrationBuilder.AddColumn<string>(
                name: "IdempotencyKey",
                table: "Histories",
                type: "character varying(100)",
                maxLength: 100,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "InitiatedBy",
                table: "Histories",
                type: "character varying(36)",
                maxLength: 36,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "LedgerEntryType",
                table: "Histories",
                type: "varchar(30)",
                nullable: true);

            migrationBuilder.AddColumn<bool>(
                name: "ManualAdjustmentFlag",
                table: "Histories",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<decimal>(
                name: "NetAmount",
                table: "Histories",
                type: "numeric(18,4)",
                nullable: false,
                defaultValue: 0m);

            migrationBuilder.AddColumn<string>(
                name: "OriginalCurrency",
                table: "Histories",
                type: "varchar(10)",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "ParentHistoryId",
                table: "Histories",
                type: "char(36)",
                maxLength: 36,
                nullable: true);

            migrationBuilder.AddColumn<DateTime>(
                name: "ProcessedAt",
                table: "Histories",
                type: "timestamp with time zone",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "RetryCount",
                table: "Histories",
                type: "integer",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.AddColumn<string>(
                name: "ReversalReason",
                table: "Histories",
                type: "character varying(255)",
                maxLength: 255,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "ReviewedBy",
                table: "Histories",
                type: "character varying(36)",
                maxLength: 36,
                nullable: true);

            migrationBuilder.AddColumn<decimal>(
                name: "RiskScore",
                table: "Histories",
                type: "numeric(5,2)",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "RoutingNumber",
                table: "Histories",
                type: "character varying(20)",
                maxLength: 20,
                nullable: true);

            migrationBuilder.AddColumn<decimal>(
                name: "RunningBalance",
                table: "Histories",
                type: "numeric(18,4)",
                nullable: false,
                defaultValue: 0m);

            migrationBuilder.AddColumn<string>(
                name: "SanctionScreeningResult",
                table: "Histories",
                type: "character varying(50)",
                maxLength: 50,
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "Tags",
                table: "Histories",
                type: "json",
                nullable: true);

            migrationBuilder.AddColumn<decimal>(
                name: "TaxAmount",
                table: "Histories",
                type: "numeric(18,4)",
                nullable: false,
                defaultValue: 0m);

            migrationBuilder.AddColumn<string>(
                name: "UserAgent",
                table: "Histories",
                type: "character varying(255)",
                maxLength: 255,
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_Histories_ParentHistoryId",
                table: "Histories",
                column: "ParentHistoryId");

            migrationBuilder.AddForeignKey(
                name: "FK_Histories_Histories_ParentHistoryId",
                table: "Histories",
                column: "ParentHistoryId",
                principalTable: "Histories",
                principalColumn: "Id");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Histories_Histories_ParentHistoryId",
                table: "Histories");

            migrationBuilder.DropIndex(
                name: "IX_Histories_ParentHistoryId",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "AdminNote",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "AmlFlag",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "ApprovalTimestamp",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "ApprovedBy",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "BankAccountNumber",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "BankCode",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "Category",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "Channel",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "ComplianceNote",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "CounterpartyAccountHolder",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "CounterpartyUserId",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "CounterpartyWalletId",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "DebitCredit",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "DeviceId",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "DisputeReference",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "DisputeStatus",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "ExchangeRate",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "ExternalReference",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "FailureReason",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "FeeAmount",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "GeoLocation",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "GrossAmount",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "IdempotencyKey",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "InitiatedBy",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "LedgerEntryType",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "ManualAdjustmentFlag",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "NetAmount",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "OriginalCurrency",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "ParentHistoryId",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "ProcessedAt",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "RetryCount",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "ReversalReason",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "ReviewedBy",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "RiskScore",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "RoutingNumber",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "RunningBalance",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "SanctionScreeningResult",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "Tags",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "TaxAmount",
                table: "Histories");

            migrationBuilder.DropColumn(
                name: "UserAgent",
                table: "Histories");

            migrationBuilder.AlterColumn<string>(
                name: "Status",
                table: "Histories",
                type: "text",
                nullable: true,
                oldClrType: typeof(string),
                oldType: "varchar(20)",
                oldNullable: true);

            migrationBuilder.AlterColumn<double>(
                name: "PreviousBalance",
                table: "Histories",
                type: "double precision",
                nullable: false,
                oldClrType: typeof(decimal),
                oldType: "numeric(18,4)");

            migrationBuilder.AlterColumn<double>(
                name: "AvailableBalance",
                table: "Histories",
                type: "double precision",
                nullable: false,
                oldClrType: typeof(decimal),
                oldType: "numeric(18,4)");

            migrationBuilder.AddColumn<double>(
                name: "Amount",
                table: "Histories",
                type: "double precision",
                nullable: false,
                defaultValue: 0.0);
        }
    }
}
