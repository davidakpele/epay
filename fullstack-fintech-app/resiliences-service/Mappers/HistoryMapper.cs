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
                Id = history.Id,
                WalletId = history.WalletId,
                UserId = history.UserId,
                SessionId = history.SessionId,
                Amount = history.Amount,
                Type = history.Type?.ToString(),
                Description = history.Description,
                Message = history.Message,
                CurrencyType = history.CurrencyType.ToString(),
                Status = history.Status?.ToString(),
                IpAddress = history.IpAddress,
                Timestamp = FormatTime(history.Timestamp),
                CreatedOn = FormatTime(history.CreatedOn),
                UpdatedOn = FormatTime(history.UpdatedOn)
            };
        }

        public static List<HistoryDTO> ToHistoryDTOs(IEnumerable<History> histories)
        {
            return histories
                .Select(h => ToHistoryDTO(h))
                .ToList();
        }

        private static string FormatTime(DateTime? time)
        {
            if (time == null)
                return string.Empty;

            return time.Value.ToString("O", CultureInfo.InvariantCulture);
        }

        private static string FormatTime(DateTime time)
        {
            return time.ToString("O", CultureInfo.InvariantCulture);
        }
    }
}