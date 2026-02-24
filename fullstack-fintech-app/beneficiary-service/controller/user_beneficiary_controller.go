package controller

import (
	"beneficiary-service/exceptions"
	"beneficiary-service/internal/domain/model"
	"beneficiary-service/internal/handler"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

type BeneficiaryController struct {
	handler *handler.BeneficiaryHandler
}

func NewBeneficiaryController(handler *handler.BeneficiaryHandler) *BeneficiaryController {
	return &BeneficiaryController{handler: handler}
}

// CreateBeneficiaryRequest represents the request payload
type CreateBeneficiaryRequest struct {
	UserID            uint                    `json:"userId" binding:"required"`
	BeneficiaryType   model.BeneficiaryType   `json:"beneficiaryType" binding:"required,oneof=bank user"`
	BeneficiaryName   string                  `json:"beneficiaryName" binding:"required"`
	Currency          string                  `json:"currency" binding:"required"`
	AccountNumber     *string                 `json:"accountNumber"`
	AccountName       *string                 `json:"accountName"`
	BankCode          *string                 `json:"bankCode"`
	BankName          *string                 `json:"bankName"`
	RecipientUsername *string                 `json:"recipientUsername"`
}

// CreateBeneficiary handles creating a new beneficiary
func (bc *BeneficiaryController) CreateBeneficiary(c *gin.Context) {
	var req CreateBeneficiaryRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid request payload",
			Details: err.Error(),
		})
		return
	}

	// Validate based on beneficiary type
	if req.BeneficiaryType == model.BeneficiaryTypeBank {
		if req.AccountNumber == nil || *req.AccountNumber == "" {
			c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
				Message: "Validation failed",
				Details: "accountNumber is required for bank beneficiaries",
			})
			return
		}
		if req.AccountName == nil || *req.AccountName == "" {
			c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
				Message: "Validation failed",
				Details: "accountName is required for bank beneficiaries",
			})
			return
		}
		if req.BankCode == nil || *req.BankCode == "" {
			c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
				Message: "Validation failed",
				Details: "bankCode is required for bank beneficiaries",
			})
			return
		}
		if req.BankName == nil || *req.BankName == "" {
			c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
				Message: "Validation failed",
				Details: "bankName is required for bank beneficiaries",
			})
			return
		}
	} else if req.BeneficiaryType == model.BeneficiaryTypeUser {
		if req.RecipientUsername == nil || *req.RecipientUsername == "" {
			c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
				Message: "Validation failed",
				Details: "recipientUsername is required for user beneficiaries",
			})
			return
		}
	}

	// Create beneficiary model
	beneficiary := &model.Beneficiary{
		UserID:          req.UserID,
		BeneficiaryType: req.BeneficiaryType,
		BeneficiaryName: req.BeneficiaryName,
		Currency:        req.Currency,
		IsActive:        true,
	}

	// Set fields based on type
	if req.BeneficiaryType == model.BeneficiaryTypeBank {
		beneficiary.AccountNumber = req.AccountNumber
		beneficiary.AccountName = req.AccountName
		beneficiary.BankCode = req.BankCode
		beneficiary.BankName = req.BankName
		beneficiary.RecipientUsername = nil
	} else {
		beneficiary.RecipientUsername = req.RecipientUsername
		beneficiary.AccountNumber = nil
		beneficiary.AccountName = nil
		beneficiary.BankCode = nil
		beneficiary.BankName = nil
	}

	// Call handler
	_ = bc.handler.CreateBeneficiary(c, beneficiary)
}

// GetBeneficiaryByUserID retrieves a single beneficiary by user ID
func (bc *BeneficiaryController) GetBeneficiaryByUserID(c *gin.Context) {
	userIDStr := c.Param("userID")
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid user ID",
			Details: "User ID must be a valid number",
		})
		return
	}

	beneficiary, err := bc.handler.GetBeneficiaryByUserID(c, uint(userID))
	if err != nil {
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"status":  "success",
		"message": "Beneficiary retrieved successfully",
		"data":    beneficiary,
	})
}

// GetAllBeneficiariesByUserID retrieves all beneficiaries for a user
func (bc *BeneficiaryController) GetAllBeneficiariesByUserID(c *gin.Context) {
	userIDStr := c.Param("userID")
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid user ID",
			Details: "User ID must be a valid number",
		})
		return
	}

	beneficiaries, err := bc.handler.GetAllBeneficiariesByUserID(c, uint(userID))
	if err != nil {
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"status":  "success",
		"message": "Beneficiaries retrieved successfully",
		"data":    beneficiaries,
	})
}

// GetBeneficiariesByType retrieves beneficiaries filtered by type
func (bc *BeneficiaryController) GetBeneficiariesByType(c *gin.Context) {
	userIDStr := c.Param("userID")
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid user ID",
			Details: "User ID must be a valid number",
		})
		return
	}

	beneficiaryType := model.BeneficiaryType(c.Query("type"))
	if beneficiaryType != model.BeneficiaryTypeBank && beneficiaryType != model.BeneficiaryTypeUser {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid beneficiary type",
			Details: "Type must be 'bank' or 'user'",
		})
		return
	}

	beneficiaries, err := bc.handler.GetBeneficiariesByType(c, uint(userID), beneficiaryType)
	if err != nil {
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"status":  "success",
		"message": "Beneficiaries retrieved successfully",
		"data":    beneficiaries,
	})
}

// SearchBeneficiaries searches beneficiaries
func (bc *BeneficiaryController) SearchBeneficiaries(c *gin.Context) {
	userIDStr := c.Param("userID")
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid user ID",
			Details: "User ID must be a valid number",
		})
		return
	}

	searchTerm := c.Query("search")
	if searchTerm == "" {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Search term required",
			Details: "Provide a search query parameter",
		})
		return
	}

	beneficiaries, err := bc.handler.SearchBeneficiaries(c, uint(userID), searchTerm)
	if err != nil {
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"status":  "success",
		"message": "Search completed successfully",
		"data":    beneficiaries,
	})
}

// UpdateBeneficiary updates a beneficiary
func (bc *BeneficiaryController) UpdateBeneficiary(c *gin.Context) {
	idStr := c.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid beneficiary ID",
			Details: "ID must be a valid number",
		})
		return
	}

	var req CreateBeneficiaryRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid request payload",
			Details: err.Error(),
		})
		return
	}

	beneficiary := &model.Beneficiary{
		ID:              uint(id),
		UserID:          req.UserID,
		BeneficiaryType: req.BeneficiaryType,
		BeneficiaryName: req.BeneficiaryName,
		Currency:        req.Currency,
	}

	if req.BeneficiaryType == model.BeneficiaryTypeBank {
		beneficiary.AccountNumber = req.AccountNumber
		beneficiary.AccountName = req.AccountName
		beneficiary.BankCode = req.BankCode
		beneficiary.BankName = req.BankName
		beneficiary.RecipientUsername = nil
	} else {
		beneficiary.RecipientUsername = req.RecipientUsername
		beneficiary.AccountNumber = nil
		beneficiary.AccountName = nil
		beneficiary.BankCode = nil
		beneficiary.BankName = nil
	}

	_ = bc.handler.UpdateBeneficiary(c, beneficiary)
}

// DeleteBeneficiary deletes a single beneficiary
func (bc *BeneficiaryController) DeleteBeneficiary(c *gin.Context) {
	idStr := c.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid beneficiary ID",
			Details: "ID must be a valid number",
		})
		return
	}

	userIDStr := c.Query("userId")
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid user ID",
			Details: "User ID must be a valid number",
		})
		return
	}

	_ = bc.handler.DeleteBeneficiary(c, uint(id), uint(userID))
}

// DeleteBeneficiariesByIDs handles deleting multiple beneficiaries
func (bc *BeneficiaryController) DeleteBeneficiariesByIDs(c *gin.Context) {
	var req struct {
		UserID uint   `json:"userId" binding:"required"`
		IDs    []uint `json:"ids" binding:"required,min=1"`
	}

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid request payload",
			Details: err.Error(),
		})
		return
	}

	_ = bc.handler.DeleteBeneficiariesByIDs(c, req.IDs, req.UserID)
}