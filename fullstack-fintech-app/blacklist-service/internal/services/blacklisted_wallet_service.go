package services

import (
	"blacklist-service/internal/domain/model"
	"blacklist-service/internal/repository"
	"time"
)

type BlackListedWalletService interface {
	IsWalletBlacklisted(walletID uint) (bool, error)
	GetBlacklistedWallet(walletID uint) (*model.BlackListedWallet, error)
	RemoveBlacklistedWallet(walletID uint) error
	FindByWalletID(walletID uint) (*model.BlackListedWallet, error)
	AddToBlackList(walletID uint, reason model.BannedReasons, isBlock bool) error
	CountBlacklistedWallets() (int64, error)
}

type blackListedWalletService struct {
	repo repository.BlackListedWalletRepository
}

func NewBlackListedWalletService(repo repository.BlackListedWalletRepository) BlackListedWalletService {
	return &blackListedWalletService{repo: repo}
}

func (s *blackListedWalletService) IsWalletBlacklisted(walletID uint) (bool, error) {
	return s.repo.ExistsByWalletID(walletID)
}

func (s *blackListedWalletService) GetBlacklistedWallet(walletID uint) (*model.BlackListedWallet, error) {
	return s.repo.FindByWalletID(walletID)
}

func (s *blackListedWalletService) RemoveBlacklistedWallet(walletID uint) error {
	return s.repo.DeleteByWalletID(walletID)
}

func (s *blackListedWalletService) FindByWalletID(walletID uint) (*model.BlackListedWallet, error) {
	return s.repo.FindByWalletID(walletID)
}

func (s *blackListedWalletService) AddToBlackList(walletID uint, reason model.BannedReasons, isBlock bool) error {
    blacklistedWallet := &model.BlackListedWallet{
        WalletID:        walletID,
        BankBannedReason: reason,
        IsBlock:         isBlock,
        Timestamp:       time.Now(),
        CreatedAt:       time.Now(),
    }
    return s.repo.Save(blacklistedWallet)
}

func (s *blackListedWalletService) CountBlacklistedWallets() (int64, error) {
    return s.repo.CountBlacklistedWallets()
}