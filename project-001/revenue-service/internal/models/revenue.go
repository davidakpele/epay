package models

import (
    "time"
    "gorm.io/gorm"
)

type Revenue struct {
    ID        uint            `gorm:"primaryKey;autoIncrement" json:"id"`
    Balances  []CurrencyBalance `gorm:"foreignKey:RevenueID" json:"balances"`
    Password  string          `gorm:"default:''" json:"-"`
    CreatedOn time.Time       `gorm:"autoCreateTime" json:"createdOn"`
    UpdatedOn time.Time       `gorm:"autoUpdateTime" json:"updatedOn"`
}

func (r *Revenue) BeforeCreate(tx *gorm.DB) error {
    r.CreatedOn = time.Now()
    r.UpdatedOn = time.Now()
    return nil
}

func (r *Revenue) BeforeUpdate(tx *gorm.DB) error {
    r.UpdatedOn = time.Now()
    return nil
}