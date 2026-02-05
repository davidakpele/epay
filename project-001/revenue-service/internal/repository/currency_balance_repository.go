package repository

import (
    "revenue-service/internal/models"
    "gorm.io/gorm"
)

type CurrencyBalanceRepository interface {
    Create(balance *models.CurrencyBalance) error
    FindByRevenueIDAndCurrency(revenueID uint, currency string) (*models.CurrencyBalance, error)
    Update(balance *models.CurrencyBalance) error
    FindByRevenueID(revenueID uint) ([]models.CurrencyBalance, error)
}

type currencyBalanceRepository struct {
    db *gorm.DB
}

func NewCurrencyBalanceRepository(db *gorm.DB) CurrencyBalanceRepository {
    return &currencyBalanceRepository{db: db}
}

func (r *currencyBalanceRepository) Create(balance *models.CurrencyBalance) error {
    return r.db.Create(balance).Error
}

func (r *currencyBalanceRepository) FindByRevenueIDAndCurrency(revenueID uint, currency string) (*models.CurrencyBalance, error) {
    var balance models.CurrencyBalance
    err := r.db.Where("revenue_id = ? AND currency_code = ?", revenueID, currency).First(&balance).Error
    
    if err != nil {
        return nil, err
    }
    
    return &balance, nil
}

func (r *currencyBalanceRepository) Update(balance *models.CurrencyBalance) error {
    return r.db.Save(balance).Error
}

func (r *currencyBalanceRepository) FindByRevenueID(revenueID uint) ([]models.CurrencyBalance, error) {
    var balances []models.CurrencyBalance
    err := r.db.Where("revenue_id = ?", revenueID).Find(&balances).Error
    return balances, err
}