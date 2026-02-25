using System;
using resiliences_service.Exceptions;
using resiliences_service.Models;

namespace resiliences_service.Helpers
{
    public static class CurrencyTypeHelper
    {
        public static CurrencyType FromString(string value)
        {
            if (Enum.TryParse<CurrencyType>(value, true, out var result))
            {
                return result;
            }

            throw new InvalidCurrencyTypeException(value);
        }
    }
}