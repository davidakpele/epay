package models

import (
    "time"
    "gorm.io/gorm"
    "github.com/shopspring/decimal"
)

type RevenueTransaction struct {
    ID              uint              `gorm:"primaryKey;autoIncrement" json:"id"`
    RevenueID       uint              `gorm:"not null" json:"revenueId"`
    Revenue         Revenue           `gorm:"foreignKey:RevenueID" json:"-"`
    TransactionType TransactionType   `gorm:"type:varchar(20);not null" json:"transactionType"`
    Amount          decimal.Decimal   `gorm:"type:numeric(38,2);not null" json:"amount"`
    Currency        CurrencyTypeStruct `gorm:"type:varchar(10);not null" json:"currency"`
    CreatedOn       time.Time         `gorm:"autoCreateTime" json:"createdOn"`
}

func (rt *RevenueTransaction) BeforeCreate(tx *gorm.DB) error {
    rt.CreatedOn = time.Now()
    return nil
}