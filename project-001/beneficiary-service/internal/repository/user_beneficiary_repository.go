package repository

import (
	"beneficiary-service/internal/domain/model"
	"errors"
	"gorm.io/gorm"
)

type BeneficiaryRepository interface {
	FindByID(id uint, userID uint) (*model.Beneficiary, error)
	FindByUserID(userID uint) (*model.Beneficiary, error)
	FindAllByUserID(userID uint) ([]model.Beneficiary, error)
	FindByType(userID uint, beneficiaryType model.BeneficiaryType) ([]model.Beneficiary, error)
	Search(userID uint, searchTerm string) ([]model.Beneficiary, error)
	FindBankByAccountNumber(userID uint, accountNumber string) (*model.Beneficiary, error)
	FindUserByUsername(userID uint, username string) (*model.Beneficiary, error)
	Save(beneficiary *model.Beneficiary) error
	Update(beneficiary *model.Beneficiary) error
	Delete(id uint, userID uint) error
	DeleteByIDs(ids []uint, userID uint) error
	HardDelete(id uint, userID uint) error
}

type beneficiaryRepository struct {
	db *gorm.DB
}

func NewBeneficiaryRepository(db *gorm.DB) BeneficiaryRepository {
	return &beneficiaryRepository{db: db}
}

// FindByID retrieves a beneficiary by ID and user ID
func (r *beneficiaryRepository) FindByID(id uint, userID uint) (*model.Beneficiary, error) {
	var beneficiary model.Beneficiary
	err := r.db.Where("id = ? AND user_id = ? AND is_active = ?", id, userID, true).
		First(&beneficiary).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("beneficiary not found")
		}
		return nil, err
	}
	return &beneficiary, nil
}

// FindByUserID retrieves the first beneficiary for a user (for backward compatibility)
func (r *beneficiaryRepository) FindByUserID(userID uint) (*model.Beneficiary, error) {
	var beneficiary model.Beneficiary
	err := r.db.Where("user_id = ? AND is_active = ?", userID, true).
		First(&beneficiary).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("beneficiary not found")
		}
		return nil, err
	}
	return &beneficiary, nil
}

// FindAllByUserID retrieves all active beneficiaries for a user
func (r *beneficiaryRepository) FindAllByUserID(userID uint) ([]model.Beneficiary, error) {
	var beneficiaries []model.Beneficiary
	err := r.db.Where("user_id = ? AND is_active = ?", userID, true).
		Order("beneficiary_name ASC").
		Find(&beneficiaries).Error
	if err != nil {
		return nil, err
	}
	return beneficiaries, nil
}

// FindByType retrieves beneficiaries filtered by type (bank or user)
func (r *beneficiaryRepository) FindByType(userID uint, beneficiaryType model.BeneficiaryType) ([]model.Beneficiary, error) {
	var beneficiaries []model.Beneficiary
	err := r.db.Where("user_id = ? AND beneficiary_type = ? AND is_active = ?", userID, beneficiaryType, true).
		Order("beneficiary_name ASC").
		Find(&beneficiaries).Error
	if err != nil {
		return nil, err
	}
	return beneficiaries, nil
}

// Search searches beneficiaries by name, account number, or username
func (r *beneficiaryRepository) Search(userID uint, searchTerm string) ([]model.Beneficiary, error) {
	var beneficiaries []model.Beneficiary
	searchPattern := "%" + searchTerm + "%"
	
	err := r.db.Where("user_id = ? AND is_active = ? AND (beneficiary_name LIKE ? OR account_number LIKE ? OR recipient_username LIKE ?)", 
		userID, true, searchPattern, searchPattern, searchPattern).
		Order("beneficiary_name ASC").
		Find(&beneficiaries).Error
	
	if err != nil {
		return nil, err
	}
	return beneficiaries, nil
}

// FindBankByAccountNumber retrieves a bank beneficiary by account number
func (r *beneficiaryRepository) FindBankByAccountNumber(userID uint, accountNumber string) (*model.Beneficiary, error) {
	var beneficiary model.Beneficiary
	err := r.db.Where("user_id = ? AND beneficiary_type = ? AND account_number = ? AND is_active = ?", 
		userID, model.BeneficiaryTypeBank, accountNumber, true).
		First(&beneficiary).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &beneficiary, nil
}

// FindUserByUsername retrieves a user beneficiary by username
func (r *beneficiaryRepository) FindUserByUsername(userID uint, username string) (*model.Beneficiary, error) {
	var beneficiary model.Beneficiary
	err := r.db.Where("user_id = ? AND beneficiary_type = ? AND recipient_username = ? AND is_active = ?", 
		userID, model.BeneficiaryTypeUser, username, true).
		First(&beneficiary).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &beneficiary, nil
}

// Save creates a new beneficiary
func (r *beneficiaryRepository) Save(beneficiary *model.Beneficiary) error {
	// Check for duplicates based on type
	if beneficiary.BeneficiaryType == model.BeneficiaryTypeBank {
		existing, err := r.FindBankByAccountNumber(beneficiary.UserID, *beneficiary.AccountNumber)
		if err != nil {
			return err
		}
		if existing != nil {
			return errors.New("beneficiary with this account number already exists")
		}
	} else if beneficiary.BeneficiaryType == model.BeneficiaryTypeUser {
		existing, err := r.FindUserByUsername(beneficiary.UserID, *beneficiary.RecipientUsername)
		if err != nil {
			return err
		}
		if existing != nil {
			return errors.New("beneficiary with this username already exists")
		}
	}

	return r.db.Create(beneficiary).Error
}

// Update updates an existing beneficiary
func (r *beneficiaryRepository) Update(beneficiary *model.Beneficiary) error {
	return r.db.Save(beneficiary).Error
}

// Delete soft deletes a beneficiary by setting is_active to false
func (r *beneficiaryRepository) Delete(id uint, userID uint) error {
	return r.db.Model(&model.Beneficiary{}).
		Where("id = ? AND user_id = ?", id, userID).
		Update("is_active", false).Error
}

// DeleteByIDs soft deletes multiple beneficiaries
func (r *beneficiaryRepository) DeleteByIDs(ids []uint, userID uint) error {
	if len(ids) == 0 {
		return errors.New("no IDs provided for deletion")
	}
	
	return r.db.Model(&model.Beneficiary{}).
		Where("id IN ? AND user_id = ?", ids, userID).
		Update("is_active", false).Error
}

// HardDelete permanently deletes a beneficiary
func (r *beneficiaryRepository) HardDelete(id uint, userID uint) error {
	return r.db.Where("id = ? AND user_id = ?", id, userID).
		Delete(&model.Beneficiary{}).Error
}