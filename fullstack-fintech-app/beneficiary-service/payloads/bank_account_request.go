package payloads

type BankAccountRequest struct {
    UserID            uint   `json:"userId"`
    AccountNumber      string `json:"accountNumber"`
    BankCode           string `json:"bankCode"`
    BankName           string `json:"bankName"`
    AccountHolderName  string `json:"accountHolderName"`
}
