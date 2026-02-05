package controller

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"bank-collection-service/internal/handler"
	"bank-collection-service/payloads"
	"bank-collection-service/exceptions"
	"bank-collection-service/internal/domain/model"
)

type BankController struct {
	handler *handler.BankHandler
}

func NewBankController(handler *handler.BankHandler) *BankController {
	return &BankController{handler: handler}
}

func (bc *BankController) CreateBankList(c *gin.Context) {
	var payload payloads.CreateBankPayload
	if err := c.ShouldBindJSON(&payload); err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid request payload",
			Details: err.Error(),
		})
		return
	}

	// Construct the bank model
	bank := model.UserBankList{
		BankCode:          payload.BankCode,
		BankName:          payload.BankName,
		AccountHolderName: payload.AccountHolderName,
		AccountNumber:     payload.AccountNumber,
		UserID:            payload.UserID,
	}
	// Call handler
	bc.handler.CreateBank(c, &bank)
}

// GetBankById extracts ID from request and calls handler
func (bc *BankController) GetBankById(c *gin.Context) {
	idStr := c.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid bank ID",
			Details: "Bank ID must be a number",
		})
		return
	}

	// Call the handler to get the bank by ID
	bank, err := bc.handler.GetBankById(c, uint(id))
	if err != nil {
		c.JSON(http.StatusNotFound, exceptions.ErrorResponse{
			Message: "Bank not found",
			Details: "No bank found with this ID",
		})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "Success", "data": bank})
}

// GetBankByAccountNumber extracts account number and calls handler
func (bc *BankController) GetBankByAccountNumber(c *gin.Context) {
	accountNumber := c.Param("accountNumber")
	bank, err := bc.handler.GetBankByAccountNumber(c, accountNumber)
	if err != nil {
		c.JSON(http.StatusNotFound, exceptions.ErrorResponse{
			Message: "Bank not found",
			Details: "No bank found with this account number",
		})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "Success", "data": bank})
}

// GetBanksByUserId extracts user ID and calls handler
func (bc *BankController) GetBanksByUserId(c *gin.Context) {
	userIdStr := c.Param("id")
	userId, err := strconv.ParseUint(userIdStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid user ID",
			Details: "User ID must be a number",
		})
		return
	}

	banks, err := bc.handler.GetBanksByUserId(c, uint(userId))
	if err != nil {
		c.JSON(http.StatusNotFound, exceptions.ErrorResponse{
			Message: "No banks found",
			Details: "User has no registered banks",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Success", "data": banks})
}

// DeleteBanksByIds extracts bank IDs from request and calls handler
func (bc *BankController) DeleteBanksByIds(c *gin.Context) {
	var payload struct {
		Ids []uint `json:"ids"`
	}
	if err := c.ShouldBindJSON(&payload); err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid request payload",
			Details: err.Error(),
		})
		return
	}

	// Make sure to capture the error from the handler function
	err := bc.handler.DeleteBanksByIds(c, payload.Ids)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to delete banks",
			Details: err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Banks deleted successfully"})
}

func (bc *BankController) FetchAllBanks(c *gin.Context) {
    banks, err := bc.handler.FetchAllBanks()
    if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to delete banks",
			Details: err.Error(),
		})
		return
	}

    c.JSON(http.StatusOK, banks)
}


func (bc *BankController) VerifyBankAccountInternally(c *gin.Context) {
	accountNumber := c.Query("accountNumber")
	bankCode := c.Query("bankCode")

	response, err := bc.handler.VerifyInternal(accountNumber, bankCode)
	if err != nil {
		if err.Error() == "account already assigned" {
			c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
				Message: "This account already been assigned to another user.",
				Details: "Already been assigned to another user.",
			})
			return
		}
		
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Error verifying bank account",
			Details: err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"status":  true,
		"message": "Account number resolved",
		"data":    response,
	})
}

func (bc *BankController) VerifyBankAccountExternal(c *gin.Context) {
	accountNumber := c.Query("accountNumber")
	bankCode := c.Query("bankCode")

	response, err := bc.handler.VerifyExternal(accountNumber, bankCode)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Error verifying bank account",
			Details: err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"status":  true,
		"message": "Account number resolved",
		"data":    response,
	})
}