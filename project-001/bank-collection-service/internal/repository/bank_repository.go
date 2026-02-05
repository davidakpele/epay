package repository

import (
	"bank-collection-service/internal/domain/model"
	"errors"

	"gorm.io/gorm"
)

type BankRepository interface {
	Create(bank *model.UserBankList) error
	FindById(id uint) (*model.UserBankList, error)
	FindByAccountNumber(accountNumber string) (*model.UserBankList, error)
	FindByUserId(userId uint) ([]model.UserBankList, error)
	DeleteByIds(ids []uint) error
	FindByAccountNumberAndBankCode(accountNumber string, bankCode string) (bool, error)
	FindInternal(accountNumber string, bankCode string) (*model.UserBankList, error)
}

type bankRepository struct {
	db *gorm.DB
}

func NewBankRepository(db *gorm.DB) BankRepository {
	return &bankRepository{db: db}
}

func (r *bankRepository) Create(bank *model.UserBankList) error {
	return r.db.Create(bank).Error
}

func (r *bankRepository) FindById(id uint) (*model.UserBankList, error) {
	var bank model.UserBankList
	err := r.db.First(&bank, id).Error
	return &bank, err
}

func (r *bankRepository) FindByAccountNumber(accountNumber string) (*model.UserBankList, error) {
	var bank model.UserBankList
	err := r.db.Where("account_number = ?", accountNumber).First(&bank).Error
	return &bank, err
}

func (r *bankRepository) FindByUserId(userId uint) ([]model.UserBankList, error) {
	var banks []model.UserBankList
	err := r.db.Where("user_id = ?", userId).Find(&banks).Error
	return banks, err
}

func (r *bankRepository) DeleteByIds(ids []uint) error {
	return r.db.Delete(&model.UserBankList{}, ids).Error
}

func (r *bankRepository) FindByAccountNumberAndBankCode(accountNumber string, bankCode string) (bool, error) {
	var count int64
	err := r.db.Model(&model.UserBankList{}).
		Where("account_number = ? AND bank_code = ?", accountNumber, bankCode).
		Count(&count).Error
	
	if err != nil {
		return false, err
	}
	
	return count > 0, nil
}

func (r *bankRepository) FindInternal(accountNumber string, bankCode string) (*model.UserBankList, error) {
	var bank model.UserBankList
	err := r.db.
		Where("account_number = ? AND bank_code = ?", accountNumber, bankCode).
		First(&bank).Error

	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, nil 
	}

	if err != nil {
		return nil, err 
	}

	return &bank, nil
}
