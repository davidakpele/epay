package payloads

type CreateBankPayload struct {
	BankCode          string `json:"bank_code" binding:"required"`
	BankName          string `json:"bank_name" binding:"required"`
	AccountHolderName string `json:"account_holder_name" binding:"required"`
	AccountNumber     string `json:"account_number" binding:"required"`
	UserID            uint   `json:"user_id" binding:"required"`
}
