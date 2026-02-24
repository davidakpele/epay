package models

import (
    "github.com/shopspring/decimal"
)

type CurrencyBalance struct {
    ID            uint            `gorm:"primaryKey;autoIncrement" json:"id"`
    RevenueID     uint            `gorm:"not null" json:"revenueId"`
    Revenue       Revenue         `gorm:"foreignKey:RevenueID" json:"-"`
    CurrencyCode  string          `gorm:"size:3;not null" json:"currencyCode"`
    CurrencySymbol string         `gorm:"size:1;not null" json:"currencySymbol"`
    Balance       decimal.Decimal `gorm:"type:numeric(38,2);not null" json:"balance"`
}