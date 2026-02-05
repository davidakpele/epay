package services

import (
	"bank-collection-service/internal/domain/model"
	"bank-collection-service/internal/repository"
	"fmt"
)

type BankService interface {
	CreateBank(bank *model.UserBankList) error
	FindById(id uint) (*model.UserBankList, error)
	FindByAccountNumber(accountNumber string) (*model.UserBankList, error)
	FindByUserId(userId uint) ([]model.UserBankList, error)
	DeleteByIds(ids []uint) error
	FindByAccountNumberAndBankCode(accountNumber string, bankCode string) (bool, error)
	GetAccountData(accountNumber string, bankCode string) (*model.UserBankList, error)
}

type bankService struct {
	repo repository.BankRepository
}

func NewBankService(repo repository.BankRepository) BankService {
	return &bankService{repo: repo}
}

// Create bank entry
func (s *bankService) CreateBank(bank *model.UserBankList) error {
	if bank.BankCode == "" || bank.BankName == "" || bank.AccountNumber == "" || bank.UserID == 0 {
		return fmt.Errorf("missing required fields")
	}
	return s.repo.Create(bank)
}

// Find bank by ID
func (s *bankService) FindById(id uint) (*model.UserBankList, error) {
	return s.repo.FindById(id)
}

// Find bank by account number
func (s *bankService) FindByAccountNumber(accountNumber string) (*model.UserBankList, error) {
	return s.repo.FindByAccountNumber(accountNumber)
}

// Find all banks by user ID
func (s *bankService) FindByUserId(userId uint) ([]model.UserBankList, error) {
	return s.repo.FindByUserId(userId)
}

// Delete bank entries by IDs
func (s *bankService) DeleteByIds(ids []uint) error {
	return s.repo.DeleteByIds(ids)
}

func (b *bankService) FindByAccountNumberAndBankCode(accountNumber string, bankCode string) (bool, error) {
	return b.repo.FindByAccountNumberAndBankCode(accountNumber, bankCode)
}

func (b *bankService) GetAccountData(accountNumber string, bankCode string) (*model.UserBankList, error) {
    return b.repo.FindInternal(accountNumber, bankCode)
}
