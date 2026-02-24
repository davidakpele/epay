package models

type TransactionType string

const (
    DEPOSIT        TransactionType = "DEPOSIT"
    WITHDRAW       TransactionType = "WITHDRAW"
    TRANSFER       TransactionType = "TRANSFER"
    CREDITED       TransactionType = "CREDITED"
    DEBITED        TransactionType = "DEBITED"
    MAINTENANCE_FEE TransactionType = "MAINTENANCE_FEE"
    TRANSACTION_FEE TransactionType = "TRANSACTION_FEE"
    SERVICE_FEE    TransactionType = "SERVICE_FEE"
)
type CurrencyTypeStruct string

const (
    USD CurrencyTypeStruct = "USD"
    EUR CurrencyTypeStruct = "EUR"
    NGN CurrencyTypeStruct = "NGN"
    GBP CurrencyTypeStruct = "GBP"
    JPY CurrencyTypeStruct = "JPY"
    AUD CurrencyTypeStruct = "AUD"
    CAD CurrencyTypeStruct = "CAD"
    CHF CurrencyTypeStruct = "CHF"
    CNY CurrencyTypeStruct = "CNY"
    INR CurrencyTypeStruct = "INR"
)

func CurrencyTypeFromString(value string) (CurrencyTypeStruct, error) {
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
        return "", &InvalidCurrencyError{Currency: value}
    }
}

type BanActions string

const (
    SuspiciousActivity           BanActions = "SUSPICIOUS_ACTIVITY"
    TermsOfServiceViolation      BanActions = "TERMS_OF_SERVICE_VIOLATION"
    FraudulentActivity           BanActions = "FRAUDULENT_ACTIVITY"
    HarassmentOrBullying         BanActions = "HARASSMENT_OR_BULLYING"
    InappropriateContent         BanActions = "INAPPROPRIATE_CONTENT"
    PlatformManipulation         BanActions = "PLATFORM_MANIPULATION"
    IdentityVerificationFailure  BanActions = "IDENTITY_VERIFICATION_FAILURE"
    SpammingOrSolicitation       BanActions = "SPAMMING_OR_SOLICITATION"
    MultipleUserReports          BanActions = "MULTIPLE_USER_REPORTS"
    ModeratorAction              BanActions = "MODERATOR_ACTION"
)

func (ba BanActions) Description() string {
    descriptions := map[BanActions]string{
        SuspiciousActivity:          "This user banned due to suspicious activity, Deposited and fast withdraw in less than one minute",
        TermsOfServiceViolation:     "This user banned for violating platform rules",
        FraudulentActivity:          "This user has engaged in fraudulent or deceptive behavior",
        HarassmentOrBullying:        "This user has harassed or bullied other users on the platform",
        InappropriateContent:        "This user has posted or shared explicit or inappropriate content",
        PlatformManipulation:        "This user has attempted to manipulate or exploit the platform's systems or algorithms",
        IdentityVerificationFailure: "This user has failed to verify their identity or provide accurate information",
        SpammingOrSolicitation:      "This user has engaged in spamming or other forms of unwanted solicitation",
        MultipleUserReports:         "This user has been reported by multiple other users for violating platform policies",
        ModeratorAction:             "This user has been banned by a moderator or administrator",
    }
    return descriptions[ba]
}

type Role string

const (
    ADMIN      Role = "ADMIN"
    SUPER_USER Role = "SUPER_USER"
    USER       Role = "USER"
)