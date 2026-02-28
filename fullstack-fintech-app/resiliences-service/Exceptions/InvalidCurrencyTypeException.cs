using System;

namespace resiliences_service.Exceptions
{
    public class InvalidCurrencyTypeException : Exception
    {
        public string Value { get; }

        public InvalidCurrencyTypeException(string value)
            : base($"unknown currency type: {value}")
        {
            Value = value;
        }
    }
}