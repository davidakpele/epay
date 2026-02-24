package repository

import (
    "revenue-service/internal/models"
    "gorm.io/gorm"
)

type TransactionRepository interface {
    Create(transaction *models.RevenueTransaction) error
    FindByRevenueID(revenueID uint) ([]models.RevenueTransaction, error)
    FindByID(id uint) (*models.RevenueTransaction, error)
}

type transactionRepository struct {
    db *gorm.DB
}

func NewTransactionRepository(db *gorm.DB) TransactionRepository {
    return &transactionRepository{db: db}
}

func (r *transactionRepository) Create(transaction *models.RevenueTransaction) error {
    return r.db.Create(transaction).Error
}

func (r *transactionRepository) FindByRevenueID(revenueID uint) ([]models.RevenueTransaction, error) {
    var transactions []models.RevenueTransaction
    err := r.db.Where("revenue_id = ?", revenueID).Order("created_on DESC").Find(&transactions).Error
    return transactions, err
}

func (r *transactionRepository) FindByID(id uint) (*models.RevenueTransaction, error) {
    var transaction models.RevenueTransaction
    err := r.db.First(&transaction, id).Error
    if err != nil {
        return nil, err
    }
    return &transaction, nil
}