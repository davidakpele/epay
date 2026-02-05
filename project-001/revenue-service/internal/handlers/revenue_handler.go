package handlers

import (
	"fmt"
	"net/http"
	"revenue-service/internal/models"
	"revenue-service/internal/service"

	"github.com/gin-gonic/gin"
	"github.com/shopspring/decimal"
)

type RevenueHandler struct {
    revenueService service.RevenueService
}

func NewRevenueHandler(revenueService service.RevenueService) *RevenueHandler {
    return &RevenueHandler{revenueService: revenueService}
}

type CreateRevenueTransferPin struct {
    Password string `json:"password" binding:"required"` 
}
 
type TransactionRequest struct {
    TransactionType string   `json:"transactionType" binding:"required"`
    Amount         float64  `json:"amount" binding:"required"`
    Currency       string   `json:"currency" binding:"required"`
    Password       *string  `json:"password,omitempty"` 
}

func (h *RevenueHandler) GetRevenue(c *gin.Context) {
    revenue, err := h.revenueService.GetRevenue()
    if err != nil {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
        return
    }

    c.JSON(http.StatusOK, revenue)
}

func (h *RevenueHandler) UpdateRevenuePassword(c *gin.Context) {
    var req CreateRevenueTransferPin
    if err := c.ShouldBindJSON(&req); err != nil {
        c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
        return
    }

    if err := h.revenueService.UpdateRevenuePassword(req.Password); err != nil {
        c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
        return
    }

    c.JSON(http.StatusOK, gin.H{"message": "Password updated successfully"})
}


func (h *RevenueHandler) ProcessTransaction(c *gin.Context) {
    var req TransactionRequest
    if err := c.ShouldBindJSON(&req); err != nil {
        c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
        return
    }

    transactionType := models.TransactionType(req.TransactionType)
    
    currency, err := models.CurrencyTypeFromString(req.Currency)
    if err != nil {
        c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
        return
    }

    amount := decimal.NewFromFloat(req.Amount).Round(2)

    if err := h.revenueService.ProcessTransaction(transactionType, amount, currency, req.Password); err != nil {
        switch err.(type) {
        case *service.InsufficientBalanceError:
            c.JSON(http.StatusBadRequest, gin.H{"error": "Insufficient balance"})
        case *service.PasswordRequiredError:
            c.JSON(http.StatusBadRequest, gin.H{"error": "Password is required for withdrawal"})
        case *service.InvalidPasswordError:
            c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid password"})
        default:
            c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
        }
        return
    }

    fmt.Printf("REVENUE SERVICE - Transaction processed successfully: %s %.2f %s\n", 
        req.TransactionType, req.Amount, req.Currency)
    
    c.JSON(http.StatusOK, gin.H{"message": "Transaction processed successfully"})
}