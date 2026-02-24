package payloads

type PaystackAccountResponse struct {
	Status  bool                   `json:"status"`
	Message string                 `json:"message"`
	Data    PaystackAccountData    `json:"data"`
}

type PaystackAccountData struct {
	AccountNumber string `json:"account_number"`
	AccountName   string `json:"account_name"`
	BankID        int    `json:"bank_id"`
}