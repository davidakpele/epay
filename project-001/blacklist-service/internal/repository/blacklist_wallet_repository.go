package repository

import (
	"blacklist-service/internal/domain/model"
	"gorm.io/gorm"
)

type BlackListedWalletRepository interface {
	ExistsByWalletID(walletID uint) (bool, error)
	FindByWalletID(walletID uint) (*model.BlackListedWallet, error)
	DeleteByWalletID(walletID uint) error
	Save(wallet *model.BlackListedWallet) error
	CountBlacklistedWallets() (int64, error)
}

type blackListedWalletRepository struct {
	db *gorm.DB
}

func NewBlackListedWalletRepository(db *gorm.DB) BlackListedWalletRepository {
	return &blackListedWalletRepository{db: db}
}

func (r *blackListedWalletRepository) ExistsByWalletID(walletID uint) (bool, error) {
	var count int64
	if err := r.db.Model(&model.BlackListedWallet{}).Where("wallet_id = ?", walletID).Count(&count).Error; err != nil {
		return false, err
	}
	return count > 0, nil
}

func (r *blackListedWalletRepository) FindByWalletID(walletID uint) (*model.BlackListedWallet, error) {
	var wallet model.BlackListedWallet
	if err := r.db.Where("wallet_id = ?", walletID).First(&wallet).Error; err != nil {
		return nil, err
	}
	return &wallet, nil
}

func (r *blackListedWalletRepository) DeleteByWalletID(walletID uint) error {
	return r.db.Where("wallet_id = ?", walletID).Delete(&model.BlackListedWallet{}).Error
}

func (r *blackListedWalletRepository) Save(wallet *model.BlackListedWallet) error {
    return r.db.Create(wallet).Error
}

func (r *blackListedWalletRepository) CountBlacklistedWallets() (int64, error) {
    var count int64
    err := r.db.Model(&model.BlackListedWallet{}).Count(&count).Error
    return count, err
}