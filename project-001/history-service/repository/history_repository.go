package repository

import (
	"history-service/models"
	"time"

	"gorm.io/gorm"
)

type HistoryRepository struct {
    DB *gorm.DB
}

func NewHistoryRepository(db *gorm.DB) *HistoryRepository {
    return &HistoryRepository{DB: db}
}

func (r *HistoryRepository) Create(history *models.History) error {
    return r.DB.Create(history).Error
}

func (r *HistoryRepository) FindByID(id string) (*models.History, error) {
    var history models.History
    err := r.DB.First(&history, "id = ?", id).Error
    return &history, err
}

func (r *HistoryRepository) FindByUserID(userID uint64) ([]models.History, error) {
    var histories []models.History
    err := r.DB.Where("user_id = ?", userID).Order("created_on DESC").Find(&histories).Error
    return histories, err
}

func (r *HistoryRepository) FindByWalletID(walletID uint64) ([]models.History, error) {
    var histories []models.History
    err := r.DB.Where("wallet_id = ?", walletID).Order("created_on DESC").Find(&histories).Error
    return histories, err
}

func (r *HistoryRepository) FindBySessionID(sessionID string) ([]models.History, error) {
    var histories []models.History
    err := r.DB.Where("session_id = ?", sessionID).Order("created_on DESC").Find(&histories).Error
    return histories, err
}

func (r *HistoryRepository) Update(history *models.History) error {
    return r.DB.Save(history).Error
}

func (r *HistoryRepository) Delete(id string) error {
    return r.DB.Delete(&models.History{}, "id = ?", id).Error
}

func (r *HistoryRepository) FindByUserIDAndCurrency(userID uint64, currencyType models.CurrencyType) ([]models.History, error) {
    var histories []models.History
    err := r.DB.Where("user_id = ? AND currency_type = ?", userID, currencyType).
        Order("created_on DESC").
        Find(&histories).Error
    return histories, err
}

func (r *HistoryRepository) FindByTimestampAfterAndWalletID(walletID uint64, timestamp time.Time) ([]models.History, error) {
    var histories []models.History
    err := r.DB.Where("wallet_id = ? AND timestamp > ?", walletID, timestamp).
        Order("timestamp DESC").
        Find(&histories).Error
    return histories, err
}

func (r *HistoryRepository) FindRecentTransactionsByUserIDLastMinutes(userID uint64, minutes int) ([]models.History, error) {
    since := time.Now().Add(-time.Duration(minutes) * time.Minute)
    var histories []models.History
    err := r.DB.Where("user_id = ? AND created_on >= ?", userID, since).
        Order("created_on DESC").
        Find(&histories).Error
    return histories, err
}

func (r *HistoryRepository) FindByUserIDWithFilters(userID uint64, startDate, endDate time.Time, transactionType string, currency string) ([]models.History, error) {
    var histories []models.History
    query := r.DB.Where("user_id = ?", userID)
    
    // Date filters
    if !startDate.IsZero() {
        query = query.Where("created_on >= ?", startDate)
    }
    if !endDate.IsZero() {
        endOfDay := endDate.Add(24 * time.Hour)
        query = query.Where("created_on < ?", endOfDay)
    }

    // Transaction type filter - skip if "ALL" or empty
    if transactionType != "" && transactionType != "ALL" {
        query = query.Where("type = ?", transactionType)
    }

    // Currency filter - skip if "ALL" or empty
    if currency != "" && currency != "ALL" {
        query = query.Where("currency_type = ?", currency)
    }
    
    // Special case: if both are "ALL", get recent 100 records
    if transactionType == "ALL" && currency == "ALL" {
        query = query.Limit(100)
    }
    
    err := query.Order("created_on DESC").Find(&histories).Error
    return histories, err
}