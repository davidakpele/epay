package model

import (
	"time"
)

// BeneficiaryType defines the type of beneficiary
type BeneficiaryType string

const (
	BeneficiaryTypeBank BeneficiaryType = "bank"
	BeneficiaryTypeUser BeneficiaryType = "user"
)

// Beneficiary represents a saved beneficiary for transfers (both bank and user)
type Beneficiary struct {
	ID               uint            `gorm:"primaryKey;autoIncrement" json:"id"`
	UserID           uint            `gorm:"not null;index:idx_user_beneficiary" json:"userId"`
	BeneficiaryType  BeneficiaryType `gorm:"type:varchar(20);not null;index:idx_user_beneficiary" json:"beneficiaryType"`
	BeneficiaryName  string          `gorm:"type:varchar(255);not null" json:"beneficiaryName"`
	Currency         string          `gorm:"type:varchar(10);not null;default:'NGN'" json:"currency"`
	
	// Bank-specific fields (NULL for user transfers)
	AccountNumber    *string         `gorm:"type:varchar(20);index:idx_account_number" json:"accountNumber,omitempty"`
	AccountName      *string         `gorm:"type:varchar(255)" json:"accountName,omitempty"`
	BankCode         *string         `gorm:"type:varchar(50)" json:"bankCode,omitempty"`
	BankName         *string         `gorm:"type:varchar(255)" json:"bankName,omitempty"`
	
	// User transfer-specific field (NULL for bank transfers)
	RecipientUsername *string        `gorm:"type:varchar(255);index:idx_recipient_username" json:"recipientUsername,omitempty"`
	
	// Metadata
	IsActive         bool            `gorm:"default:true" json:"isActive"`
	CreatedOn        time.Time       `gorm:"autoCreateTime" json:"createdOn"`
	UpdatedOn        time.Time       `gorm:"autoUpdateTime" json:"updatedOn"`
}

// TableName specifies the table name for GORM
func (Beneficiary) TableName() string {
	return "beneficiaries"
}