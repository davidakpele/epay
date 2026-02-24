package models

type CurrencyType string

const (
    USD CurrencyType = "USD"
    EUR CurrencyType = "EUR"
    NGN CurrencyType = "NGN"
    GBP CurrencyType = "GBP"
    JPY CurrencyType = "JPY"
    AUD CurrencyType = "AUD"
    CAD CurrencyType = "CAD"
    CHF CurrencyType = "CHF"
    CNY CurrencyType = "CNY"
    INR CurrencyType = "INR"
)

func CurrencyTypeFromString(value string) (CurrencyType, error) {
    switch value {
    case "USD":
        return USD, nil
    case "EUR":
        return EUR, nil
    case "NGN":
        return NGN, nil
    case "GBP":
        return GBP, nil
    case "JPY":
        return JPY, nil
    case "AUD":
        return AUD, nil
    case "CAD":
        return CAD, nil
    case "CHF":
        return CHF, nil
    case "CNY":
        return CNY, nil
    case "INR":
        return INR, nil
    default:
        return "", &InvalidCurrencyTypeError{Value: value}
    }
}

type InvalidCurrencyTypeError struct {
    Value string
}

func (e *InvalidCurrencyTypeError) Error() string {
    return "unknown currency type: " + e.Value
}