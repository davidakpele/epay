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
	FindByAccountNumberAndBankNameForUser(accountNumber string, bankName string) (bool, error)
	GetAccountData(accountNumber string, bankCode string) (*model.UserBankList, error)
}

type bankService struct {
	repo repository.BankRepository
}

func NewBankService(repo repository.BankRepository) BankService {
	return &bankService{repo: repo}
}

func (s *bankService) CreateBank(bank *model.UserBankList) error {
	if bank.BankCode == "" || bank.BankName == "" || bank.AccountNumber == "" || bank.UserID == 0 {
		return fmt.Errorf("missing required fields")
	}
	return s.repo.Create(bank)
}

func (s *bankService) FindById(id uint) (*model.UserBankList, error) {
	return s.repo.FindById(id)
}

func (s *bankService) FindByAccountNumber(accountNumber string) (*model.UserBankList, error) {
	return s.repo.FindByAccountNumber(accountNumber)
}

func (s *bankService) FindByUserId(userId uint) ([]model.UserBankList, error) {
	return s.repo.FindByUserId(userId)
}

func (s *bankService) DeleteByIds(ids []uint) error {
	return s.repo.DeleteByIds(ids)
}

func (s *bankService) FindByAccountNumberAndBankCode(accountNumber string, bankCode string) (bool, error) {
	return s.repo.FindByAccountNumberAndBankCode(accountNumber, bankCode)
}

func (s *bankService) FindByAccountNumberAndBankNameForUser(accountNumber string, bankName string) (bool, error) {
	return s.repo.FindByAccountNumberAndBankNameForUser(accountNumber, bankName)
}

func (s *bankService) GetAccountData(accountNumber string, bankCode string) (*model.UserBankList, error) {
	return s.repo.FindInternal(accountNumber, bankCode)
}