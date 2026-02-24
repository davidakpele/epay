package router

import (
	"history-service/handlers"
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

func SetupRouter(historyHandler *handlers.HistoryHandler) *gin.Engine {
	router := gin.Default()

	// CORS configuratio
    router.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"*"},
		AllowMethods:     []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Authorization", "Content-Type"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
		MaxAge:           12 * time.Hour,
	}))
	// Health check route
	router.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status": "OK",
			"service": "history-service",
		})
	})

	// API routes
	api := router.Group("/history")
	{
		api.GET("/:id", historyHandler.GetHistoryByID)
		api.DELETE("/:id", historyHandler.DeleteHistoryByID) 
		api.GET("/user/:userId", historyHandler.GetHistoryByUserID)
		api.GET("/wallet/:walletId", historyHandler.GetHistoryByWalletID)
		api.GET("/session/:sessionId", historyHandler.GetHistoryBySessionID)
		api.POST("/create/deposit", historyHandler.CreateDeposit)
		api.POST("/create/swap", historyHandler.CreateSwap)
		api.GET("/user/:userId/currency/:currency", historyHandler.GetHistoryByUserIDAndCurrency)
		api.POST("/create/withdrawal", historyHandler.CreateWithdrawal)
		api.POST("/create/feature", historyHandler.CreateFeatureHistory)
		api.GET("/user/:userId/transactions/recent", historyHandler.GetRecentTransactionsByUserIDLastMinutes)
		api.GET("/wallet/:walletId/transactions/timestamp", historyHandler.GetWalletTransactionsAfterTimestamp)
		api.GET("/user/:userId/filter", historyHandler.GetFilteredHistoriesByUserID)

		// New Cache endpoints
		api.GET("/cache/user/:userId", historyHandler.GetCachedUserHistories)
		api.GET("/cache/all", historyHandler.ListAllCachedHistories)
		api.POST("/cache/refresh", historyHandler.RefreshCache)
		api.POST("/cache/invalidate/user/:userId", historyHandler.InvalidateUserCache)
		api.POST("/cache/invalidate/all", historyHandler.InvalidateAllCache)
		api.GET("/cache/stats", historyHandler.GetCacheStats)
		api.GET("/cache/health", historyHandler.CacheHealthCheck)
	}

	return router
}