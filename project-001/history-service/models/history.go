package models

import (
    "time"
    "github.com/google/uuid"
    "gorm.io/gorm"
)

type History struct {
    ID           string    `gorm:"type:char(36);primary_key" json:"id"`
    WalletID     uint64    `gorm:"not null" json:"walletId"`
    UserID       uint64    `gorm:"not null" json:"userId"`
    SessionID    string    `json:"sessionId"`
    TransactionID string   `json:"transactionId"`
    ReferenceID   string   `json:"referenceNo"`
    TerminalID   string    `json:"terminalId"`
    ERID         string    `json:"erId"`
    AccountHolder string   `json:"accountHolder"`
    PreviousBalance float64 `json:"previousBalance"`
    AvailableBalance float64 `json:"availableBalance"`
    Amount       float64   `json:"amount"`
    Type         string    `gorm:"type:varchar(50)" json:"type"`
    Description  string    `json:"description"`
    Message      string    `json:"message"`
    CurrencyType string    `gorm:"type:varchar(10);not null" json:"currencyType"`
    Status       string    `json:"status"`
    IPAddress    string    `gorm:"column:ip_address;size:45" json:"ipAddress"`
    Timestamp    *time.Time `json:"timestamp"`
    CreatedOn         time.Time      `gorm:"autoCreateTime" json:"-"`
	UpdatedOn         time.Time      `gorm:"autoUpdateTime" json:"-"`
}

func (history *History) BeforeCreate(tx *gorm.DB) error {
    if history.ID == "" {
        history.ID = uuid.New().String()
    }
    history.CreatedOn = time.Now()
    history.UpdatedOn = time.Now()
    return nil
}

func (history *History) BeforeUpdate(tx *gorm.DB) error {
    history.UpdatedOn = time.Now()
    return nil
}