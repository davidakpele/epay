package controller

import (
	"blacklist-service/internal/handler"
	"net/http"

	"github.com/gin-gonic/gin"
)

type BlackListedWalletController struct {
	handler *handler.BlackListedWalletHandler
}

func NewBlackListedWalletController(handler *handler.BlackListedWalletHandler) *BlackListedWalletController {
	return &BlackListedWalletController{handler: handler}
}

// Check if wallet is blacklisted
func (bc *BlackListedWalletController) CheckWalletBlacklistStatus(c *gin.Context) {
	bc.handler.CheckWalletBlacklistStatus(c)
}

// Remove wallet from blacklist
func (bc *BlackListedWalletController) RemoveBlacklistedWallet(c *gin.Context) {
	bc.handler.RemoveBlacklistedWallet(c)
}

// Add wallet to blacklist
func (bc *BlackListedWalletController) AddToBlackList(c *gin.Context) {
	bc.handler.AddToBlackList(c)
}

func (bc *BlackListedWalletController) CountBlacklistedWallets(c *gin.Context) {
    bc.handler.CountBlacklistedWallets(c)
}