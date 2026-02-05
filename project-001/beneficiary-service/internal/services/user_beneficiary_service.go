package services

import (
	"beneficiary-service/internal/domain/model"
	"beneficiary-service/internal/repository"
	"errors"
)

type BeneficiaryService interface {
	GetBeneficiaryByID(id uint, userID uint) (*model.Beneficiary, error)
	GetBeneficiaryByUserID(userID uint) (*model.Beneficiary, error)
	GetAllBeneficiariesByUserID(userID uint) ([]model.Beneficiary, error)
	GetBeneficiariesByType(userID uint, beneficiaryType model.BeneficiaryType) ([]model.Beneficiary, error)
	SearchBeneficiaries(userID uint, searchTerm string) ([]model.Beneficiary, error)
	CreateBeneficiary(beneficiary *model.Beneficiary) error
	UpdateBeneficiary(beneficiary *model.Beneficiary) error
	DeleteBeneficiary(id uint, userID uint) error
	DeleteBeneficiariesByIDs(ids []uint, userID uint) error
}

type beneficiaryService struct {
	repo repository.BeneficiaryRepository
}

func NewBeneficiaryService(repo repository.BeneficiaryRepository) BeneficiaryService {
	return &beneficiaryService{repo: repo}
}

// GetBeneficiaryByID retrieves a beneficiary by ID
func (s *beneficiaryService) GetBeneficiaryByID(id uint, userID uint) (*model.Beneficiary, error) {
	beneficiary, err := s.repo.FindByID(id, userID)
	if err != nil {
		return nil, err
	}
	return beneficiary, nil
}

// GetBeneficiaryByUserID retrieves the first beneficiary for a user
func (s *beneficiaryService) GetBeneficiaryByUserID(userID uint) (*model.Beneficiary, error) {
	beneficiary, err := s.repo.FindByUserID(userID)
	if err != nil {
		return nil, err
	}
	return beneficiary, nil
}

// GetAllBeneficiariesByUserID retrieves all beneficiaries for a user
func (s *beneficiaryService) GetAllBeneficiariesByUserID(userID uint) ([]model.Beneficiary, error) {
	beneficiaries, err := s.repo.FindAllByUserID(userID)
	if err != nil {
		return nil, err
	}
	return beneficiaries, nil
}

// GetBeneficiariesByType retrieves beneficiaries filtered by type
func (s *beneficiaryService) GetBeneficiariesByType(userID uint, beneficiaryType model.BeneficiaryType) ([]model.Beneficiary, error) {
	if beneficiaryType != model.BeneficiaryTypeBank && beneficiaryType != model.BeneficiaryTypeUser {
		return nil, errors.New("invalid beneficiary type. Must be 'bank' or 'user'")
	}

	beneficiaries, err := s.repo.FindByType(userID, beneficiaryType)
	if err != nil {
		return nil, err
	}
	return beneficiaries, nil
}

// SearchBeneficiaries searches beneficiaries by name, account number, or username
func (s *beneficiaryService) SearchBeneficiaries(userID uint, searchTerm string) ([]model.Beneficiary, error) {
	if searchTerm == "" {
		return nil, errors.New("search term cannot be empty")
	}

	beneficiaries, err := s.repo.Search(userID, searchTerm)
	if err != nil {
		return nil, err
	}
	return beneficiaries, nil
}

// CreateBeneficiary creates a new beneficiary with validation
func (s *beneficiaryService) CreateBeneficiary(beneficiary *model.Beneficiary) error {
	// Validate beneficiary type
	if beneficiary.BeneficiaryType != model.BeneficiaryTypeBank && 
	   beneficiary.BeneficiaryType != model.BeneficiaryTypeUser {
		return errors.New("invalid beneficiary type. Must be 'bank' or 'user'")
	}

	// Validate required fields based on type
	if beneficiary.BeneficiaryType == model.BeneficiaryTypeBank {
		if beneficiary.AccountNumber == nil || *beneficiary.AccountNumber == "" {
			return errors.New("account number is required for bank beneficiaries")
		}
		if beneficiary.AccountName == nil || *beneficiary.AccountName == "" {
			return errors.New("account name is required for bank beneficiaries")
		}
		if beneficiary.BankCode == nil || *beneficiary.BankCode == "" {
			return errors.New("bank code is required for bank beneficiaries")
		}
		if beneficiary.BankName == nil || *beneficiary.BankName == "" {
			return errors.New("bank name is required for bank beneficiaries")
		}

		// Check for duplicate bank account
		existing, err := s.repo.FindBankByAccountNumber(beneficiary.UserID, *beneficiary.AccountNumber)
		if err != nil {
			return errors.New("failed to check for duplicate beneficiary")
		}
		if existing != nil {
			return errors.New("beneficiary with this account number already exists")
		}
	} else if beneficiary.BeneficiaryType == model.BeneficiaryTypeUser {
		if beneficiary.RecipientUsername == nil || *beneficiary.RecipientUsername == "" {
			return errors.New("recipient username is required for user beneficiaries")
		}

		// Check for duplicate username
		existing, err := s.repo.FindUserByUsername(beneficiary.UserID, *beneficiary.RecipientUsername)
		if err != nil {
			return errors.New("failed to check for duplicate beneficiary")
		}
		if existing != nil {
			return errors.New("beneficiary with this username already exists")
		}
	}

	// Validate common fields
	if beneficiary.BeneficiaryName == "" {
		return errors.New("beneficiary name is required")
	}
	if beneficiary.Currency == "" {
		return errors.New("currency is required")
	}

	// Save beneficiary
	err := s.repo.Save(beneficiary)
	if err != nil {
		return errors.New("failed to create beneficiary")
	}

	return nil
}

// UpdateBeneficiary updates an existing beneficiary
func (s *beneficiaryService) UpdateBeneficiary(beneficiary *model.Beneficiary) error {
	// Verify beneficiary exists
	existing, err := s.repo.FindByID(beneficiary.ID, beneficiary.UserID)
	if err != nil {
		return errors.New("beneficiary not found")
	}
	if existing == nil {
		return errors.New("beneficiary not found")
	}

	// Update the beneficiary
	err = s.repo.Update(beneficiary)
	if err != nil {
		return errors.New("failed to update beneficiary")
	}

	return nil
}

// DeleteBeneficiary soft deletes a beneficiary
func (s *beneficiaryService) DeleteBeneficiary(id uint, userID uint) error {
	// Verify beneficiary exists
	_, err := s.repo.FindByID(id, userID)
	if err != nil {
		return errors.New("beneficiary not found")
	}

	// Delete beneficiary
	err = s.repo.Delete(id, userID)
	if err != nil {
		return errors.New("failed to delete beneficiary")
	}

	return nil
}

// DeleteBeneficiariesByIDs deletes multiple beneficiaries
func (s *beneficiaryService) DeleteBeneficiariesByIDs(ids []uint, userID uint) error {
	if len(ids) == 0 {
		return errors.New("no beneficiary IDs provided")
	}

	err := s.repo.DeleteByIDs(ids, userID)
	if err != nil {
		return errors.New("failed to delete beneficiaries")
	}

	return nil
}