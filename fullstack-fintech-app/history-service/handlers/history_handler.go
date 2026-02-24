// handlers/history_handler.go
package handlers

import (
	"context"
	"fmt"
	"history-service/models"
	"history-service/repository"
	"history-service/services"
	"history-service/utils"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
)

type HistoryHandler struct {
	repo         *repository.HistoryRepository
	cacheService *services.HistoryCacheService
}

func NewHistoryHandler(repo *repository.HistoryRepository, cacheService *services.HistoryCacheService) *HistoryHandler {
	return &HistoryHandler{
		repo:         repo,
		cacheService: cacheService,
	}
}

func (h *HistoryHandler) GetCachedUserHistories(c *gin.Context) {
	userIDStr := c.Param("userId")
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid user ID"})
		return
	}

	ctx := c.Request.Context()
	cachedHistories, err := h.cacheService.GetCachedUserHistories(ctx, userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to get cached data",
			"details": err.Error(),
		})
		return
	}

	if cachedHistories != nil {
		c.JSON(http.StatusOK, gin.H{
			"data":   cachedHistories,
			"cached": true,
			"count":  len(cachedHistories),
			"source": "redis_cache",
		})
		return
	}

	histories, err := h.repo.FindByUserID(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to fetch data from database"})
		return
	}

	go func() {
		cacheCtx := context.Background()
		if err := h.cacheService.CacheUserHistories(cacheCtx, userID); err != nil {
			fmt.Printf("Failed to cache user histories: %v\n", err)
		}
	}()

	c.JSON(http.StatusOK, gin.H{
		"data":   histories,
		"cached": false,
		"count":  len(histories),
		"source": "database",
	})
}

func (h *HistoryHandler) ListAllCachedHistories(c *gin.Context) {
	ctx := c.Request.Context()
	cachedHistories, err := h.cacheService.GetCachedAllHistories(ctx)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to get cached data",
			"details": err.Error(),
		})
		return
	}

	if cachedHistories != nil {
		c.JSON(http.StatusOK, gin.H{
			"data":   cachedHistories,
			"cached": true,
			"count":  len(cachedHistories),
			"source": "redis_cache",
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"data":    []models.History{},
		"cached":  false,
		"count":   0,
		"source":  "cache_miss",
		"message": "No cached data available. Cache will be populated by scheduled job.",
	})
}

func (h *HistoryHandler) RefreshCache(c *gin.Context) {
	go h.cacheService.WarmupCache()
	
	c.JSON(http.StatusAccepted, gin.H{
		"message": "Cache refresh initiated in background",
		"warmup_in_progress": h.cacheService.IsWarmupInProgress(),
	})
}

func (h *HistoryHandler) InvalidateUserCache(c *gin.Context) {
	userIDStr := c.Param("userId")
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid user ID"})
		return
	}

	ctx := c.Request.Context()
	if err := h.cacheService.InvalidateUserCache(ctx, userID); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to invalidate user cache",
			"details": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"message": "User cache invalidated successfully",
		"user_id": userID,
	})
}

func (h *HistoryHandler) InvalidateAllCache(c *gin.Context) {
	ctx := c.Request.Context()
	if err := h.cacheService.InvalidateAllCache(ctx); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to invalidate all cache",
			"details": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"message": "All history caches invalidated successfully",
	})
}

func (h *HistoryHandler) GetCacheStats(c *gin.Context) {
	ctx := c.Request.Context()
	stats, err := h.cacheService.GetCacheStats(ctx)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Failed to get cache statistics",
			"details": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"stats": stats,
	})
}

func (h *HistoryHandler) CacheHealthCheck(c *gin.Context) {
	ctx := c.Request.Context()
	if err := h.cacheService.HealthCheck(ctx); err != nil {
		c.JSON(http.StatusServiceUnavailable, gin.H{
			"status":  "unhealthy",
			"service": "cache",
			"error":   err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"status":  "healthy",
		"service": "cache",
	})
}

func (h *HistoryHandler) GetHistoryByID(c *gin.Context) {
	id := c.Param("id")
	history, err := h.repo.FindByID(id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "History not found"})
		return
	}
	c.JSON(http.StatusOK, history)
}

func (h *HistoryHandler) DeleteHistoryByID(c *gin.Context) {
	id := c.Param("id")
	if err := h.repo.Delete(id); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to delete history"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "History deleted successfully"})
}

func (h *HistoryHandler) GetHistoryByUserID(c *gin.Context) {
	userIDStr := c.Param("userId")
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid user ID"})
		return
	}

	histories, err := h.repo.FindByUserID(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to fetch histories"})
		return
	}
	c.JSON(http.StatusOK, histories)
}

func (h *HistoryHandler) GetHistoryByWalletID(c *gin.Context) {
	walletIDStr := c.Param("walletId")
	walletID, err := strconv.ParseUint(walletIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid wallet ID"})
		return
	}

	histories, err := h.repo.FindByWalletID(walletID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to fetch histories"})
		return
	}
	c.JSON(http.StatusOK, histories)
}

func (h *HistoryHandler) GetHistoryBySessionID(c *gin.Context) {
	sessionID := c.Param("sessionId")
	histories, err := h.repo.FindBySessionID(sessionID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to fetch histories"})
		return
	}
	c.JSON(http.StatusOK, histories)
}

func (h *HistoryHandler) CreateDeposit(c *gin.Context) {
	var history models.History
	if err := c.ShouldBindJSON(&history); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid input"})
		return
	}

	if history.TransactionID == "" {
		history.TransactionID = utils.GenerateUniqueRecipientId()
	}
	if history.SessionID == "" {
		history.SessionID = utils.GenerateSessionID()
	}
	if history.ReferenceID == "" {
		history.ReferenceID = utils.GenerateReferenceNo()
	}
	if history.TerminalID == "" {
		history.TerminalID = utils.GenerateTerminalID()
	}
	if history.ERID == "" {
		history.ERID = utils.GenerateERID()
	}

	now := time.Now()
	history.Timestamp = &now
	history.Type = "DEPOSIT"
	history.Status = "SUCCESS"

	if err := h.repo.Create(&history); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create deposit history"})
		return
	}

	go func() {
		ctx := context.Background()
		h.cacheService.InvalidateUserCache(ctx, history.UserID)
	}()

	c.JSON(http.StatusCreated, history)
}

func (h *HistoryHandler) CreateSwap(c *gin.Context) {
	var history models.History
	if err := c.ShouldBindJSON(&history); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid input"})
		return
	}

	if history.TransactionID == "" {
		history.TransactionID = utils.GenerateUniqueRecipientId()
	}
	if history.SessionID == "" {
		history.SessionID = utils.GenerateSessionID()
	}
	if history.ReferenceID == "" {
		history.ReferenceID = utils.GenerateReferenceNo()
	}
	if history.TerminalID == "" {
		history.TerminalID = utils.GenerateTerminalID()
	}
	if history.ERID == "" {
		history.ERID = utils.GenerateERID()
	}

	now := time.Now()
	history.Timestamp = &now
	history.Type = "SWAP"
	history.Status = "SUCCESS"
	if err := h.repo.Create(&history); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create swap history"})
		return
	}
	go func() {
		ctx := context.Background()
		h.cacheService.InvalidateUserCache(ctx, history.UserID)
	}()

	c.JSON(http.StatusCreated, history)
}

func (h *HistoryHandler) GetHistoryByUserIDAndCurrency(c *gin.Context) {
	userIDStr := c.Param("userId")
	currency := c.Param("currency")
	
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid user ID"})
		return
	}

	histories, err := h.repo.FindByUserIDAndCurrency(userID, models.CurrencyType(currency))
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to fetch histories"})
		return
	}
	c.JSON(http.StatusOK, histories)
}

func (h *HistoryHandler) CreateWithdrawal(c *gin.Context) {
	
	var history models.History
	if err := c.ShouldBindJSON(&history); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid input"})
		return
	}
	if err := h.repo.Create(&history); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create withdrawal history"})
		return
	}

	go func() {
		ctx := context.Background()
		h.cacheService.InvalidateUserCache(ctx, history.UserID)
	}()

	c.JSON(http.StatusCreated, history)
}

func (h *HistoryHandler) CreateFeatureHistory(c *gin.Context) {
	var history models.History
	if err := c.ShouldBindJSON(&history); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid input"})
		return
	}
	if history.TransactionID == "" {
		history.TransactionID = utils.GenerateUniqueRecipientId()
	}
	if history.SessionID == "" {
		history.SessionID = utils.GenerateSessionID()
	}
	if history.ReferenceID == "" {
		history.ReferenceID = utils.GenerateReferenceNo()
	}
	if history.TerminalID == "" {
		history.TerminalID = utils.GenerateTerminalID()
	}
	if history.ERID == "" {
		history.ERID = utils.GenerateERID()
	}

	now := time.Now()
	history.Timestamp = &now
	history.Type = "FEATURE"
	history.Status = "SUCCESS"
	if err := h.repo.Create(&history); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create feature history"})
		return
	}

	go func() {
		ctx := context.Background()
		h.cacheService.InvalidateUserCache(ctx, history.UserID)
	}()

	c.JSON(http.StatusCreated, history)
}

func (h *HistoryHandler) GetRecentTransactionsByUserIDLastMinutes(c *gin.Context) {
	userIDStr := c.Param("userId")
	minutesStr := c.DefaultQuery("minutes", "60")
	
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid user ID"})
		return
	}

	minutes, err := strconv.Atoi(minutesStr)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid minutes parameter"})
		return
	}

	histories, err := h.repo.FindRecentTransactionsByUserIDLastMinutes(userID, minutes)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to fetch recent transactions"})
		return
	}
	c.JSON(http.StatusOK, histories)
}

func (h *HistoryHandler) GetWalletTransactionsAfterTimestamp(c *gin.Context) {
	walletIDStr := c.Param("walletId")
	timestampStr := c.Query("timestamp")
	
	walletID, err := strconv.ParseUint(walletIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid wallet ID"})
		return
	}

	timestamp, err := time.Parse(time.RFC3339, timestampStr)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid timestamp format. Use RFC3339"})
		return
	}

	histories, err := h.repo.FindByTimestampAfterAndWalletID(walletID, timestamp)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to fetch wallet transactions"})
		return
	}
	c.JSON(http.StatusOK, histories)
}


func (h *HistoryHandler) GetFilteredHistoriesByUserID(c *gin.Context) {
	userIDStr := c.Param("userId")
	startDateStr := c.Query("fromDate")
	endDateStr := c.Query("toDate")
	transactionType := c.Query("transactionType")
	currency := c.Query("currency")
	
	userID, err := strconv.ParseUint(userIDStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid user ID"})
		return
	}

	var startDate, endDate time.Time
	if startDateStr != "" {
		startDate, err = time.Parse("2006-01-02", startDateStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid fromDate format. Use YYYY-MM-DD"})
			return
		}
	}
	if endDateStr != "" {
		endDate, err = time.Parse("2006-01-02", endDateStr)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid toDate format. Use YYYY-MM-DD"})
			return
		}
	}
	
	histories, err := h.repo.FindByUserIDWithFilters(userID, startDate, endDate, transactionType, currency)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to fetch filtered histories"})
		return
	}
	
	c.JSON(http.StatusOK, gin.H{
		"data": histories,
	})
}