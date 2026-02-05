package handler

import (
	"blacklist-service/exceptions"
	"blacklist-service/internal/domain/model"
	"blacklist-service/internal/services"
	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"net/http"
	"strconv"
)

type BlackListedWalletHandler struct {
	service services.BlackListedWalletService
}

func NewBlackListedWalletHandler(service services.BlackListedWalletService) *BlackListedWalletHandler {
	return &BlackListedWalletHandler{service: service}
}

func (h *BlackListedWalletHandler) CheckWalletBlacklistStatus(c *gin.Context) {
	walletID, err := strconv.ParseUint(c.Param("walletID"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid wallet ID",
			Details: "Wallet ID must be a valid number",
		})
		return
	}
	wallet, err := h.service.FindByWalletID(uint(walletID))
	if err != nil || wallet == nil {
		c.JSON(http.StatusOK, false)
		return
	}

	isBlacklisted, err := h.service.IsWalletBlacklisted(uint(walletID))
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to check blacklist status",
			Details: err.Error(),
		})
		return
	}
	c.JSON(http.StatusOK, isBlacklisted)
}

func (h *BlackListedWalletHandler) RemoveBlacklistedWallet(c *gin.Context) {
	walletID, err := strconv.ParseUint(c.Param("walletID"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid wallet ID",
			Details: "Wallet ID must be a valid number",
		})
		return
	}
	requestID := uuid.New().String()
	wallet, err := h.service.FindByWalletID(uint(walletID))
	if err != nil || wallet == nil {
		c.JSON(http.StatusNotFound, exceptions.ErrorResponse{
			Message: "Data not found",
			Details: "No blacklisted wallet found.",
		})
		return
	}
	err = h.service.RemoveBlacklistedWallet(uint(walletID))
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to remove wallet from blacklist",
			Details: err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"message":   "Wallet removed from blacklist successfully",
		"requestID": requestID,
	})
}

func (h *BlackListedWalletHandler) AddToBlackList(c *gin.Context) {
	var request struct {
		WalletID         uint                `json:"wallet_id"`
		BankBannedReason model.BannedReasons `json:"bank_banned_reason"`
		IsBlock          bool                `json:"is_block"`
	}
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid request payload",
			Details: err.Error(),
		})
		return
	}
	validReasons := map[model.BannedReasons]bool{
		model.SuspiciousActivity:          true,
		model.TermsOfServiceViolation:     true,
		model.FraudulentActivity:          true,
		model.HarassmentOrBullying:        true,
		model.InappropriateContent:        true,
		model.PlatformManipulation:        true,
		model.IdentityVerificationFailure: true,
		model.SpammingOrSolicitation:      true,
		model.MultipleUserReports:         true,
		model.ModeratorAction:             true,
	}

	if !validReasons[request.BankBannedReason] {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Invalid ban reason",
			Details: "The provided reason is not recognized.",
		})
		return
	}
	isBlacklisted, err := h.service.IsWalletBlacklisted(request.WalletID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to check blacklist status",
			Details: err.Error(),
		})
		return
	}

	if isBlacklisted {
		c.JSON(http.StatusConflict, exceptions.ErrorResponse{
			Message: "Wallet already blacklisted",
			Details: "This wallet is already in the blacklist.",
		})
		return
	}

	err = h.service.AddToBlackList(request.WalletID, request.BankBannedReason, request.IsBlock)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to add wallet to blacklist",
			Details: err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, exceptions.ErrorResponse{
		Message: "Wallet successfully added to blacklist",
		Details: "User account has been successfully blacklisted, This user wallet can not perform any withdraws till user clear his profile by contact our customer service.",
	})
}


func (h *BlackListedWalletHandler) CountBlacklistedWallets(c *gin.Context) {
    count, err := h.service.CountBlacklistedWallets()
    if err != nil {
        c.JSON(http.StatusInternalServerError, gin.H{
            "message": "Failed to count blacklisted wallets",
            "error": err.Error(),
        })
        return
    }
    c.JSON(http.StatusOK, gin.H{
        "count": count,
        "status": "success",
    })
}