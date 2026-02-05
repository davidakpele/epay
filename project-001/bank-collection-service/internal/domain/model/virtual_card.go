package model

import "time"

type VirtualCard struct {
	ID             uint      `json:"id" gorm:"primaryKey;autoIncrement"`
	CardID         string    `json:"card_id"`
	UserID         uint    `json:"user_id"`
	UserWalletID   uint    `json:"user_wallet_id"`
	CardNumber     string    `json:"card_number"`
	CVV            string    `json:"cvv"`
	ExpiryMonth    int       `json:"expiry_month"`
	ExpiryYear     int       `json:"expiry_year"`
	CardHolderName string    `json:"card_holder_name"`
	CardType       string    `json:"card_type"`
	CardTheme      string    `json:"card_theme"`
	Status         string    `json:"status"`
	SpendingLimit  float64   `json:"spending_limit"`
	CurrentBalance float64   `json:"current_balance"`
	Currency       string    `json:"currency"`
	BillingAddress string    `json:"billing_address"`
	CreatedAt      time.Time `json:"created_at"`
	UpdatedAt      time.Time `json:"updated_at"`
}

type CreateVirtualCardRequest struct {
	ID             string  `json:"id"`
	UserID         uint  `json:"user_id"`
	UserWalletID   uint  `json:"user_wallet_id"`
	CardHolderName string  `json:"card_holder_name"`
	CardType       string  `json:"card_type"`
	CardTheme      string  `json:"card_theme"`
	Currency       string  `json:"currency"`
	BillingAddress string  `json:"billing_address"`
}

type VirtualCardsListResponse struct {
	Message string          `json:"message"`
	Cards   []VirtualCard   `json:"cards"`
	Count   int             `json:"count"`
}

type VirtualCardResponse struct {
	Message string       `json:"message"`
	Card    *VirtualCard `json:"card"`
	Status string       `json:"status"`
}
