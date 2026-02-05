package model

import (
	"time"
)

type UserBankList struct {
	ID                uint      `json:"id" gorm:"primaryKey"`
	BankCode          string    `json:"bank_code"`
	BankName          string    `json:"bank_name"`
	AccountHolderName string    `json:"account_holder_name"`
	AccountNumber     string    `json:"account_number" gorm:"unique;not null"`
	UserID            uint      `json:"user_id"`
	CreatedOn         time.Time `json:"created_on" gorm:"autoCreateTime"`
	UpdatedOn         time.Time `json:"updated_on" gorm:"autoUpdateTime"`
}
