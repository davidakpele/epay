using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using resiliences_service.DTOs;
using resiliences_service.Models;

namespace resiliences_service.Mappers
{
    public static class HistoryMapper
    {
        public static HistoryDTO ToHistoryDTO(History history)
        {
            return new HistoryDTO
            {
                // Core Identity
                Id = history.Id,
                WalletId = (long)history.WalletId,
                UserId = (long)history.UserId,
                SessionId = history.SessionId ?? string.Empty,
                TransactionId = history.TransactionId ?? string.Empty,
                ReferenceNo = history.ReferenceId ?? string.Empty,
                TerminalId = history.TerminalId ?? string.Empty,
                ErId = history.ErId ?? string.Empty,
                AccountHolder = history.AccountHolder ?? string.Empty,
                
                // Transaction Details
                TransactionType = history.Type?.ToString() ?? string.Empty,
                Description = history.Description ?? string.Empty,
                Message = history.Message ?? string.Empty,
                CurrencyType = history.CurrencyType,
                Status = history.Status ?? string.Empty,
                IpAddress = history.IpAddress ?? string.Empty,
                Timestamp = FormatTime(history.Timestamp),
                
                // Financial Amounts
                GrossAmount = (decimal)history.GrossAmount,
                FeeAmount = (decimal)history.FeeAmount,
                TaxAmount = (decimal)history.TaxAmount,
                NetAmount = (decimal)history.NetAmount,
                PreviousBalance = (decimal)history.PreviousBalance,
                AvailableBalance = (decimal)history.AvailableBalance,
                RunningBalance = (decimal)history.RunningBalance,
                
                // Double-Entry Accounting
                DebitCredit = history.DebitCredit?.ToString() ?? string.Empty,
                LedgerEntryType = history.LedgerEntryType?.ToString() ?? string.Empty,
                
                // Counterparty & Routing
                CounterpartyWalletId = history.CounterpartyWalletId.HasValue ? (long)history.CounterpartyWalletId.Value : 0,
                CounterpartyUserId = history.CounterpartyUserId.HasValue ? (long)history.CounterpartyUserId.Value : 0,
                CounterpartyAccountHolder = history.CounterpartyAccountHolder ?? string.Empty,
                BankCode = history.BankCode ?? string.Empty,
                BankAccountNumber = history.BankAccountNumber ?? string.Empty,
                RoutingNumber = history.RoutingNumber ?? string.Empty,
                ExternalReference = history.ExternalReference ?? string.Empty,
                
                // Multi-Currency
                OriginalCurrency = history.OriginalCurrency ?? string.Empty,
                ExchangeRate = history.ExchangeRate ?? 0,
                
                // Reversal & Disputes
                ParentHistoryId = history.ParentHistoryId ?? string.Empty,
                ReversalReason = history.ReversalReason ?? string.Empty,
                DisputeStatus = history.DisputeStatus?.ToString() ?? string.Empty,
                DisputeReference = history.DisputeReference ?? string.Empty,
                
                // Idempotency & Retry
                IdempotencyKey = history.IdempotencyKey ?? string.Empty,
                RetryCount = history.RetryCount,
                FailureReason = history.FailureReason ?? string.Empty,
                ProcessedAt = FormatTime(history.ProcessedAt),
                
                // Channel & Device
                Channel = history.Channel?.ToString() ?? string.Empty,
                DeviceId = history.DeviceId ?? string.Empty,
                UserAgent = history.UserAgent ?? string.Empty,
                GeoLocation = history.GeoLocation ?? string.Empty,
                
                // Compliance & Risk
                RiskScore = history.RiskScore ?? 0,
                AmlFlag = history.AmlFlag,
                SanctionScreeningResult = history.SanctionScreeningResult ?? string.Empty,
                ComplianceNote = history.ComplianceNote ?? string.Empty,
                ReviewedBy = history.ReviewedBy ?? string.Empty,
                
                // Admin Audit
                InitiatedBy = history.InitiatedBy ?? string.Empty,
                ApprovedBy = history.ApprovedBy ?? string.Empty,
                ApprovalTimestamp = FormatTime(history.ApprovalTimestamp),
                AdminNote = history.AdminNote ?? string.Empty,
                ManualAdjustmentFlag = history.ManualAdjustmentFlag,
                
                // Metadata
                Category = history.Category?.ToString() ?? string.Empty,
                Tags = history.Tags ?? string.Empty,
                
                // System Fields
                CreatedOn = FormatTime(history.CreatedOn),
                UpdatedOn = FormatTime(history.UpdatedOn)
            };
        }

        public static List<HistoryDTO> ToHistoryDTOs(IEnumerable<History> histories) =>
            histories.Select(ToHistoryDTO).ToList();

        private static string FormatTime(DateTime? time) =>
            time?.ToString("O", CultureInfo.InvariantCulture) ?? string.Empty;

        private static string FormatTime(DateTime time) =>
            time.ToString("O", CultureInfo.InvariantCulture);
    }
}