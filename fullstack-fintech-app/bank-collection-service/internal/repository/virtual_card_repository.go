package repository

import (
	"bank-collection-service/internal/domain/model"
	"gorm.io/gorm"
)

type VirtualCardRepository struct {
	db *gorm.DB
}

func NewVirtualCardRepository(db *gorm.DB) *VirtualCardRepository {
	return &VirtualCardRepository{db: db}
}

func (r *VirtualCardRepository) Create(card *model.VirtualCard) error {
	return r.db.Create(card).Error
}

func (r *VirtualCardRepository) CardNumberExists(cardNumber string) (bool, error) {
	var count int64
	err := r.db.Model(&model.VirtualCard{}).Where("card_number = ?", cardNumber).Count(&count).Error
	return count > 0, err
}

func (r *VirtualCardRepository) FindByCardID(cardID string) (*model.VirtualCard, error) {
	var card model.VirtualCard
	err := r.db.Where("card_id = ?", cardID).First(&card).Error
	if err != nil {
		return nil, err
	}
	return &card, nil
}

func (r *VirtualCardRepository) FindAllByUserID(userID uint) ([]model.VirtualCard, error) {
	var cards []model.VirtualCard
	err := r.db.Where("user_id = ?", userID).Find(&cards).Error
	if err != nil {
		return nil, err
	}
	return cards, nil
}

func (r *VirtualCardRepository) FindByUserID(userID uint, cardType string) (*model.VirtualCard, error) {
	var card model.VirtualCard
	err := r.db.Where("user_id = ? AND card_type = ?", userID, cardType).First(&card).Error
	if err != nil {
		return nil, err
	}
	return &card, nil
}