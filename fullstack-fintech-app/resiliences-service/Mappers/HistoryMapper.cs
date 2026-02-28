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
                Id               = history.Id,
                WalletId         = (long)history.WalletId,
                UserId           = (long)history.UserId,
                SessionId        = history.SessionId        ?? string.Empty,
                TransactionId    = history.TransactionId    ?? string.Empty,
                ReferenceNo      = history.ReferenceId      ?? string.Empty,
                TerminalId       = history.TerminalId       ?? string.Empty,
                ErId             = history.ErId             ?? string.Empty,
                AccountHolder    = history.AccountHolder    ?? string.Empty,
                PreviousBalance  = (decimal)history.PreviousBalance,
                AvailableBalance = (decimal)history.AvailableBalance,
                Amount           = (decimal)history.Amount,
                TransactionType  = history.Type?.ToString() ?? string.Empty,
                Description      = history.Description      ?? string.Empty,
                Message          = history.Message          ?? string.Empty,
                CurrencyType     = history.CurrencyType,
                Status           = history.Status           ?? string.Empty,
                IpAddress        = history.IpAddress        ?? string.Empty,
                Timestamp        = FormatTime(history.Timestamp),
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