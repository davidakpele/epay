package handler

import (
	"beneficiary-service/exceptions"
	"beneficiary-service/internal/domain/model"
	"beneficiary-service/internal/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

type BeneficiaryHandler struct {
	service services.BeneficiaryService
}

func NewBeneficiaryHandler(service services.BeneficiaryService) *BeneficiaryHandler {
	return &BeneficiaryHandler{service: service}
}

func (h *BeneficiaryHandler) CreateBeneficiary(c *gin.Context, beneficiary *model.Beneficiary) error {
	err := h.service.CreateBeneficiary(beneficiary)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to create beneficiary",
			Details: err.Error(),
		})
		return err
	}
	
	c.JSON(http.StatusOK, gin.H{
		"status":  "success",
		"message": "Beneficiary created successfully",
		"data":    beneficiary,
	})
	return nil
}

// GetBeneficiaryByUserID retrieves the first beneficiary for a user
func (h *BeneficiaryHandler) GetBeneficiaryByUserID(c *gin.Context, userID uint) (*model.Beneficiary, error) {
	beneficiary, err := h.service.GetBeneficiaryByUserID(userID)
	if err != nil {
		c.JSON(http.StatusNotFound, exceptions.ErrorResponse{
			Message: "Beneficiary not found",
			Details: err.Error(),
		})
		return nil, err
	}
	return beneficiary, nil
}

// GetAllBeneficiariesByUserID retrieves all beneficiaries for a user
func (h *BeneficiaryHandler) GetAllBeneficiariesByUserID(c *gin.Context, userID uint) ([]model.Beneficiary, error) {
	beneficiaries, err := h.service.GetAllBeneficiariesByUserID(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to retrieve beneficiaries",
			Details: err.Error(),
		})
		return []model.Beneficiary{}, err
	}
	return beneficiaries, nil
}

// GetBeneficiariesByType retrieves beneficiaries filtered by type
func (h *BeneficiaryHandler) GetBeneficiariesByType(c *gin.Context, userID uint, beneficiaryType model.BeneficiaryType) ([]model.Beneficiary, error) {
	beneficiaries, err := h.service.GetBeneficiariesByType(userID, beneficiaryType)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid request",
			Details: err.Error(),
		})
		return []model.Beneficiary{}, err
	}
	return beneficiaries, nil
}

// SearchBeneficiaries searches beneficiaries
func (h *BeneficiaryHandler) SearchBeneficiaries(c *gin.Context, userID uint, searchTerm string) ([]model.Beneficiary, error) {
	beneficiaries, err := h.service.SearchBeneficiaries(userID, searchTerm)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Search failed",
			Details: err.Error(),
		})
		return []model.Beneficiary{}, err
	}
	return beneficiaries, nil
}

// UpdateBeneficiary updates a beneficiary
func (h *BeneficiaryHandler) UpdateBeneficiary(c *gin.Context, beneficiary *model.Beneficiary) error {
	err := h.service.UpdateBeneficiary(beneficiary)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to update beneficiary",
			Details: err.Error(),
		})
		return err
	}
	
	c.JSON(http.StatusOK, gin.H{
		"status":  "success",
		"message": "Beneficiary updated successfully",
		"data":    beneficiary,
	})
	return nil
}

// DeleteBeneficiary deletes a single beneficiary
func (h *BeneficiaryHandler) DeleteBeneficiary(c *gin.Context, id uint, userID uint) error {
	err := h.service.DeleteBeneficiary(id, userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to delete beneficiary",
			Details: err.Error(),
		})
		return err
	}
	
	c.JSON(http.StatusOK, gin.H{
		"status":  "success",
		"message": "Beneficiary deleted successfully",
	})
	return nil
}

// DeleteBeneficiariesByIDs handles deleting multiple beneficiaries by their IDs
func (h *BeneficiaryHandler) DeleteBeneficiariesByIDs(c *gin.Context, ids []uint, userID uint) error {
	err := h.service.DeleteBeneficiariesByIDs(ids, userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to delete beneficiaries",
			Details: err.Error(),
		})
		return err
	}
	
	c.JSON(http.StatusOK, gin.H{
		"status":  "success",
		"message": "Beneficiaries deleted successfully",
	})
	return nil
}